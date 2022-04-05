package com.xpay.gateway.callback.conts;

/**
 * @description 过滤器执行顺序的常量配置类，值越小，越早执行
 * @author: chenyf
 */
public class FilterOrder {
    //从-50到-41是非业务相关的前置请求全局过滤器，如：黑名单过滤器
    public final static int IP_BLACKLIST_FILTER = -50;//IP黑名单过滤器
    public final static int BIZ_OFF_FILTER = -49;//业务停用过滤器(主要用在某些业务线整体维护时使用)

    //从-40到-31是对请求体做相应处理的全局过滤器
    public final static int REQUEST_READ_FILTER = -40; //读取请求体内容，并缓存
    public final static int REQUEST_PARAM_CHECK_FILTER = -39; //请求参数校验
    public final static int REQUEST_AUTH_FILTER = -38;  //请求参数签名验证
    public final static int REQUEST_MODIFY_FILTER = -37; //修改请求体
    public final static int REWRITE_PATH_FILTER = -36; //根据请求参数中method的值修改请求uri


    //从-30到-11是GatewayFilterFactory的顺序



    //最后一个前置过滤器，是对响应体做相应处理的全局过滤器，在过滤器中把官方框架的响应体输出类换成自定义的，以达到修改响应体的目的，
    // 这个替换动作需要在 NettyWriteResponseFilter 之前执行
    public final static int RESPONSE_MODIFY_FILTER = -2;

    //以上是前置过滤器(pre-filter)，以下是后置过滤器(post-filter)，两者的设置不一样，前置过滤器的执行顺序是先执行自定义的逻辑，最后再通过：
    // return chain.filter(exchange) 来让过滤器链继续往前走，而后置过滤器的定义需要像这样：return chain.filter(exchange).then(...后置过滤器的处理逻辑...)
    // 因为后置过滤器是在 NettyWriteResponseFilter 之后执行的，所以不再可以修改响应体内容，但可以做一些日志打印、RT记录等等

}
