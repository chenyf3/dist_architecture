package com.xpay.demo.sentinel.web;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.xpay.common.utils.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 关于 @SentinelResource 注解的参数解释如下：
 * value：   资源名称
 * entryType：标识该资源是入口流量还是出口流量，设置为 IN 是为了统计整个系统的流量水平，防止系统被打垮，用以自我保护的一种方式。设置为 OUT
 *           一方面是为了保护第三方系统，另一方面也可以保护自己的系统，比如我们系统依赖了一个生成订单号的接口，而这个接口是核心服务，如果我们的
 *           服务是非核心应用，就需要对订单号生成接口进行限流保护；假设我们的服务是核心应用，而依赖的第三方应用老是超时，那这时可以通过设置依赖的
 *           服务的 rt 来进行降级，这样就不至于让第三方服务把我们的系统拖垮。简单点说服务提供方用IN，接口调用发起方选OUT
 * resourceType：表示资源的类型，例如 Dubbo RPC、Web MVC 或者 API Gateway 网关
 * blockHandler：处理 BlockException 的函数名称，该函数需要是 public，返回类型需要与原方法相同，入参数类型也需要和原方法相同，并且最后添加
 *              一个 BlockException 类型的参数
 * blockHandlerClass：默认情况下 blockHandler 参数指定的函数需要和原方法在同一个类中，如果希望使用其他类的函数，则需要指定 blockHandlerClass
 *                   为对应的类的 Class 对象，注意对应的函数必需为  public static 函数，否则无法解析
 * fallback：用于在抛出异常的时候提供 fallback 处理逻辑。fallback 函数可以针对所有类型的异常（除了 exceptionsToIgnore 里面排除掉的异常类型）进行处理
 * fallbackClass：fallbackClass的应用和blockHandlerClass类似，fallback 函数默认需要和原方法在同一个类中。若希望使用其他类的函数，则可以
 *               指定 fallbackClass 为对应的类的 Class 对象，注意对应的函数必需为 public static 函数，否则无法解析
 * defaultFallback：默认的 fallback 函数名称，通常用于通用的 fallback 逻辑（即可以用于很多服务或方法）。默认 fallback 函数可以针对所有类型
 *                  的异常（除了 exceptionsToIgnore 里面排除掉的异常类型）进行处理。若同时配置了 fallback 和 defaultFallback，则只有
 *                  fallback 会生效。defaultFallback 函数签名有如下要求：
 *                      1. 返回值类型必须与原函数返回值类型一致；
 *                      2. 方法参数列表需要为空，或者可以额外多一个 Throwable 类型的参数用于接收对应的异常。
 *                      3. defaultFallback 函数默认需要和原方法在同一个类中。若希望使用其他类的函数，则可以指定 fallbackClass 为对应的类
 *                        的 Class 对象，注意对应的函数必需为 static 函数，否则无法解析
 * exceptionsToIgnore：用于指定哪些异常被排除掉，不会计入异常统计中，也不会进入 fallback 逻辑中，而是会原样抛出。
 *
 * 注意：
 * 如果 blockHandler 和 fallback 都进行了配置，则被限流降级而抛出 BlockException 时只会进入 blockHandler 处理逻辑。其他异常则会进入
 * fallback 处理逻辑，若未配置 blockHandler、fallback 和 defaultFallback，则被限流降级时会将 BlockException 直接抛出
 * （若方法本身未定义 throws BlockException 则会被 JVM 包装一层 UndeclaredThrowableException）
 * 更多官方文档请参考：https://github.com/alibaba/Sentinel/wiki/%E6%B3%A8%E8%A7%A3%E6%94%AF%E6%8C%81
 */
@Component
public class DemoService {
    @Autowired
    RemoteService remoteService;

    /**
     * 流控测试(单机阈值)
     *
     * 注：限流应该是发生的提供者端，消费者端应该不会有限流的情况存在，消费者端应该只有熔断降级的情况存在，当然这里的生产端和消费端是对限流的那个资源而言的
     *
     * 流控规则：
     * 针对来源：default
     * 阈值类型：QPS
     * 单机阈值：3
     *
     * @return
     */
    @SentinelResource(value = "flowLimit", entryType = EntryType.IN, blockHandler="flowBlockHandler")
    public String flow() {
        return "flow";
    }
    //发生限流时的处理函数
    public String flowBlockHandler(BlockException e){
        return "发生flow限流";
    }

    /**
     * 流控测试(集群阈值)
     *
     * 流控规则：
     * 针对来源：default
     * 阈值类型：QPS
     * 集群阈值：3
     * 是否集群：是
     * 集群阈值模式：总体阈值
     * 失败退化：是
     * @return
     */
    @SentinelResource(value = "flowClusterLimit", entryType = EntryType.IN, blockHandler="flowClusterBlockHandler")
    public String flowCluster() {
        return "flowCluster";
    }
    //发生集群限流时的处理函数
    public String flowClusterBlockHandler(BlockException e){
        return "发生flowCluster限流";
    }

    /**
     * 流控测试(集群阈值)，主要测试大数据量时的效率
     *
     * 流控规则：
     * 针对来源：default
     * 阈值类型：QPS
     * 集群阈值：2000000
     * 是否集群：是
     * 集群阈值模式：总体阈值
     * 失败退化：否
     * @return
     */
    @SentinelResource(
            value = "flowClusterManyLimit",
            entryType = EntryType.IN,
            blockHandler="flowClusterManyBlockHandler",
            fallback = "flowClusterManyFallback"
    )
    public String flowClusterMany() {
        return "flowClusterMany";
    }
    //发生集群限流时的处理函数
    public String flowClusterManyBlockHandler(BlockException e){
        return "发生flowClusterMany限流";
    }
    public String flowClusterManyFallback(Throwable e){
        return "flowClusterManyFallback";
    }

    /**
     * 流控测试(集群阈值failover本地)，主要测试大数据量时的效率
     *
     * 流控规则：
     * 针对来源：default
     * 阈值类型：QPS
     * 集群阈值：2000000
     * 是否集群：是
     * 集群阈值模式：总体阈值
     * 失败退化：是
     * @return
     */
    @SentinelResource(
            value = "flowClusterFailoverManyLimit",
            entryType = EntryType.IN,
            blockHandler="flowClusterFailoverManyBlockHandler",
            fallback = "flowClusterFailoverManyFallback"
    )
    public String flowClusterFailoverMany() {
        return "flowClusterFailoverMany";
    }
    //发生集群限流时的处理函数
    public String flowClusterFailoverManyBlockHandler(BlockException e){
        return "发生flowClusterFailoverMany限流";
    }
    public String flowClusterFailoverManyFallback(Throwable e){
        return "flowClusterFailoverManyFallback";
    }

    /**
     * 测试熔断，熔断的理解就是 达到一定的条件后(慢RT比例、异常比例、异常数)会触发熔断机制，然后熔断会持续一段时间，在这段时间内，所有的请求会进入
     * 到 blockHandler 指定的函数里面，等到熔断时间过了之后，又会放一点请求过去，如果放过去的请求还是无法正常处理，将会继续处于熔断状态，如果放
     * 过去的请求已经可以正常处理，则会结束熔断，恢复正常处理流程
     *
     * 注：熔断降级应该是发生的消费者端，提供者端应该不会有熔断降级的情况存在，提供者端应该只有限流的情况存在
     *
     * 熔断规则配置：
     * 策略：慢调用比例
     * 最大RT：300ms
     * 比例阈值：0.3
     * 熔断时长：1s
     * 最小请求数：5
     * 统计时长1000ms
     *
     * @param param
     * @return
     */
    @SentinelResource(
            value = "degradeTest",
            entryType = EntryType.OUT,
            blockHandler = "degradeHandler",
            fallback = "degradeFallback"
    )
    public String degrade(String param) {
        int rand = RandomUtil.getInt(1, 1000);
        if(rand % 3 == 0){
            remoteService.call();
        }else if(rand % 3 == 1){
            throw new RuntimeException("特意抛出异常，模拟业务异常，以检测fallback策略！");
        }
        return "degrade";
    }
    //发生熔断时的处理函数
    public String degradeHandler(String param, BlockException e){
        return "degradeOccur";
    }
    //发生降级时的处理函数
    public String degradeFallback(String param, Throwable e){
        return "degradeFallback";
    }
}
