package com.xpay.web.api.common.manager.impl;

import com.xpay.common.utils.JWTUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.starter.plugin.client.RedisClient;
import com.xpay.web.api.common.config.Constants;
import com.xpay.web.api.common.config.WebApiProperties;
import com.xpay.web.api.common.manager.TokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 通过Redis存储和验证token的实现类
 * @date 2018/04/09
 */
public class TokenManagerImpl implements TokenManager {
	private Logger logger = LoggerFactory.getLogger(TokenManagerImpl.class);
	private WebApiProperties webProperties;
	private RedisClient redisClient;

	public TokenManagerImpl(WebApiProperties webProperties, RedisClient redisClient){
		this.webProperties = webProperties;
		this.redisClient = redisClient;
	}

	/**
	 *
	 * @param loginName
	 * @param loginIp
	 * @param userAgent
	 * @return
	 */
	@Override
	public String createAndStoreToken(String loginName, String loginIp, String userAgent, String mchNo) {
		//1.先删除旧的token(如果存在的话)
		this.deleteTokenFromServer(loginName);

		//2.封装构建token需要的参数，然后调用工具类生成token
		Map<String, String> claims = new HashMap<>();
		claims.put(CLAIM_KEY_USER_ANENT, userAgent);
		claims.put(CLAIM_KEY_MCH_NO, mchNo);
		Integer expiredSec = webProperties.getTokenExpiredSec();
		String token;
		try{
			token = JWTUtil.createToken(loginName, loginIp, expiredSec, webProperties.getTokenSecretKey(), claims);
		}catch(Throwable e){
			logger.error("loginName = {} 创建Token出现异常", loginName, e);
			throw e;
		}

		//3.token成功生成，存储到redis中并设置过期时间
		int serverExpireSec = this.getTokenServerExpireSec();
		this.storeTokenToServer(loginName, token, serverExpireSec);
		return token;
	}

	/**
	 * 校验token有效性，并刷新在服务端的过期时间
	 * @param token
	 * @return
	 */
	@Override
	public Map<String, String> verifyAndRenewToken(String token) {
		//1.验证token本身是否有效
		Map<String, String> claims;
		try{
			claims = JWTUtil.verifyToken(token, webProperties.getTokenSecretKey());
		} catch (Throwable e) {
			logger.error("token校验不通过，token = {}", token, e);
			return null;
		}

		//2.验证token是否在服务端有效
		String loginName = getLoginName(claims);
		String currentToken = this.getTokenFromServer(loginName);
		if (StringUtil.isEmpty(currentToken)) {
			logger.error("loginName: {} 会话超时", loginName);
			return null;
		} else if( ! currentToken.equals(token) ) {
			logger.error("loginName: {} 已在其他地方登录", loginName);
			return null;
		}

		//3.验证通过，说明此用户进行了一次有效操作，延长token在服务端的过期时间
		int expireSec = getTokenServerExpireSec();
		this.storeTokenToServer(loginName, token, expireSec);
		return claims;
	}

	@Override
	public Boolean validateClientInfo(Map<String, String> claims, String clientIp, String userAgent, String... args){
		String loginName = JWTUtil.getSubject(claims);
//		String ip = JWTUtil.getAudience(claims);
		String agent = claims.get(CLAIM_KEY_USER_ANENT);
//		if(ip == null || !ip.equals(clientIp)){
//			logger.error("loginName: {} 与登录时的IP不一致", loginName);
//			return false;
//		}else
		if(agent == null || !agent.equals(userAgent)){
			logger.error("loginName: {} 与登录时的客户端不一致", loginName);
			return false;
		}
		return true;
	}

	@Override
	public String getLoginName(Map<String, String> claims){
		return JWTUtil.getSubject(claims);
	}

	/**
	 * 把token存储到服务端
	 * @param key
	 * @param expireSec
	 */
	public boolean storeTokenToServer(String key, String token, int expireSec){
		try{
			String storeKey = getStoreKey(key);
			redisClient.set(storeKey, token, expireSec);
			return true;
		}catch(Exception e){
			logger.error("存储token到redis时出现异常 key: {} token: ", key, token, e);
			return false;
		}
	}

	/**
	 * 从服务端获取token
	 * @param key
	 */
	public String getTokenFromServer(String key){
		try{
			String storeKey = getStoreKey(key);
			return redisClient.get(storeKey);
		}catch(Exception e){
			logger.error("从redis中获取token时出现异常 key: {} ", key, e);
			return null;
		}
	}

	/**
	 * 清除服务端存储的token
	 * @param key 登录用户的id
	 */
	public void deleteTokenFromServer(String key){
		try{
			String storeKey = getStoreKey(key);
			redisClient.del(storeKey);
		}catch(Exception e){
			logger.error("从redis中删除token时出现异常 key: {}", key, e);
		}
	}

	private String getStoreKey(String loginName){
		return Constants.CACHE_LOGIN_TOKEN_FLAG + webProperties.getAppName() + ":" + loginName;
	}

	private int getTokenServerExpireSec(){
		Integer expireSec = webProperties.getTokenServerExpiredSec();
		if(expireSec == null || expireSec <= 0){
			expireSec = WebApiProperties.DEFAULT_SERVER_EXPIRE_SEC;
		}
		return expireSec;
	}
}
