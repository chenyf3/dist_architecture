package com.xpay.starter.plugin.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 邮件发送器的配置属性
 * @author chenyf
 */
@ConfigurationProperties(prefix = "email")
public class MailProperties {
	private Boolean enable = Boolean.TRUE;
	public List<Sender> senders;

	public Boolean getEnable() {
		return enable;
	}

	public void setEnable(Boolean enable) {
		this.enable = enable;
	}

	public List<Sender> getSenders() {
		return senders;
	}

	public void setSenders(List<Sender> senders) {
		this.senders = senders;
	}

	public static class Sender {
		private final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

		/**
		 * SMTP server host. For instance, `smtp.example.com`.
		 */
		private String host;

		/**
		 * SMTP server port.
		 */
		private Integer port;

		/**
		 * Login user of the SMTP server.
		 */
		private String username;

		/**
		 * Login password of the SMTP server.
		 */
		private String password;

		/**
		 * 邮件发送者描述
		 */
		private String desc;

		/**
		 * Protocol used by the SMTP server.
		 */
		private String protocol = "smtp";

		/**
		 * Default MimeMessage encoding.
		 */
		private Charset defaultEncoding = DEFAULT_CHARSET;

		/**
		 * Additional JavaMail Session properties.
		 */
		private Map<String, String> properties = new HashMap<>();

		public String getHost() {
			return this.host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public Integer getPort() {
			return this.port;
		}

		public void setPort(Integer port) {
			this.port = port;
		}

		public String getUsername() {
			return this.username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return this.password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getDesc() {
			return desc;
		}

		public void setDesc(String desc) {
			this.desc = desc;
		}

		public String getProtocol() {
			return this.protocol;
		}

		public void setProtocol(String protocol) {
			this.protocol = protocol;
		}

		public Charset getDefaultEncoding() {
			return this.defaultEncoding;
		}

		public void setDefaultEncoding(Charset defaultEncoding) {
			this.defaultEncoding = defaultEncoding;
		}

		public void setProperties(Map<String, String> properties) {
			this.properties = properties;
		}

		public Map<String, String> getProperties() {
			//配置一些默认超时时间，避免一直阻塞
			if(!this.properties.containsKey("mail.smtp.connectiontimeout")){//与邮件服务器建立连接的时间超时时间，单位毫秒
				this.properties.put("mail.smtp.connectiontimeout", "10000");
			}
			if(!this.properties.containsKey("mail.smtp.timeout")){//接收邮件的超时时间，单位毫秒
				this.properties.put("mail.smtp.timeout", "10000");
			}
			if(!this.properties.containsKey("mail.smtp.writetimeout")){//邮件发送时间，单位毫秒
				this.properties.put("mail.smtp.writetimeout", "10000");
			}
			return this.properties;
		}
	}
}
