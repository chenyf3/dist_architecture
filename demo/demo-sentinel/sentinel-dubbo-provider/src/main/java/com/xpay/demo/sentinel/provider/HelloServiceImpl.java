package com.xpay.demo.sentinel.provider;

import com.xpay.demo.dubbo.api.HelloService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService
public class HelloServiceImpl implements HelloService {
    @Autowired
    HelloBiz helloBiz;

    /**
     * 说明：引入了 sentinel-apache-dubbo-adapter 依赖包之后，就不要再加 {@link com.alibaba.csp.sentinel.annotation.SentinelResource} 注解了，
     * 因为引入了 sentinel-apache-dubbo-adapter 包之后会自动为Dubbo服务提供者方法加上sentinel流控保护，这些资源会通过 {@link com.alibaba.csp.sentinel.adapter.dubbo.SentinelDubboProviderFilter}
     * 来处理，而 @SentinelResource 注解定义的资源会被{@link com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect}
     * 这个类中定义的切面包含在里面，而这个类里面对于 {@link com.alibaba.csp.sentinel.slots.block.BlockException} 的处理方式是
     * 寻找当前注解上的 blockHandler、fallback、defaultFallback等属性的配置，如果发现没有任何一个属性有配置，就会直接把 BlockException
     * 往上抛出，而 BlockException 上有个 {@link com.alibaba.csp.sentinel.slots.block.BlockException#rule} 属性，而这个属性的子类
     * 就是 FlowRule、DegradeRule、SystemRule 等等，这些类都没有实现 java.io.Serializable 接口，也就不能被Dubbo序列化，从而会报错。
     * 当然，除了Dubbo服务提供者的接口实现类之外，在其他类中要想实现流控还是可以加@SentinelResource注解的，比如，{@link HelloBiz#testHello()}
     * @return
     */
    @Override
    public String sayHello() {
        return helloBiz.sayHello();
    }

    /**
     * 引入了 sentinel-apache-dubbo-adapter 依赖包之后，不要再在方法上添加 @SentinelResource 注解了，只需要到dashboard添加相关流控规则就好了
     * @return
     */
    @Override
    public String helloWorld(){
        return helloBiz.helloWorld();
    }
}
