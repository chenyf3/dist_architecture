package com.xpay.demo.sequence;

import com.xpay.facade.sequence.service.SequenceFacade;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/demo")
public class DemoController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @DubboReference
    SequenceFacade sequenceFacade;

    @RequestMapping(value = "/getRedisId")
    public Mono<List<Long>> getRedisId(Integer count) {
        List<Long> ids = new ArrayList<>();
        try{
            if(count == null || count <= 0){
                count = 1;
            }
            long start = System.currentTimeMillis();
            for(int i=0; i<count; i++){
                long id = sequenceFacade.nextRedisId("testRedisSingleId");
                ids.add(id);
            }
            long cost = System.currentTimeMillis()-start;
            System.out.println("getRedisId count="+count+",cost="+cost+"(ms)");
        }catch(Exception e){
            e.printStackTrace();
        }
        return Mono.justOrEmpty(ids);
    }

    @RequestMapping(value = "/listRedisId")
    public Mono<List<Long>> listRedisId(int count) {
        List<Long> idList = new ArrayList<>();
        try{
            long start = System.currentTimeMillis();
            idList = sequenceFacade.nextRedisId("testRedisBatchId", count);
            long cost = System.currentTimeMillis()-start;
            System.out.println("listRedisId count="+count+",cost="+cost+"(ms)");
        }catch(Exception e){
            e.printStackTrace();
        }
        return Mono.just(idList);
    }

    @RequestMapping(value = "/getSnowId")
    public Mono<List<Long>> getSnowId(Integer count) {
        List<Long> ids = new ArrayList<>();
        try{
            if(count == null || count <= 0){
                count = 1;
            }
            long start = System.currentTimeMillis();
            for(int i=0; i<count; i++){
                long id = sequenceFacade.nextSnowId();
                ids.add(id);
            }
            long cost = System.currentTimeMillis()-start;
            System.out.println("getSnowId count="+count+",cost="+cost+"(ms)");
        }catch(Exception e){
            e.printStackTrace();
        }

        return Mono.justOrEmpty(ids);
    }

    @RequestMapping(value = "/listSnowId")
    public Mono<List<Long>> listSnowId(int count) {
        List<Long> idList = new ArrayList<>();
        try{
            long start = System.currentTimeMillis();
            idList = sequenceFacade.nextSnowId(count);
            long cost = System.currentTimeMillis()-start;
            System.out.println("listSnowId count="+count+",cost="+cost+"(ms)");
        }catch(Exception e){
            e.printStackTrace();
        }
        return Mono.just(idList);
    }

    @RequestMapping(value = "/getSegmentId")
    public Mono<List<Long>> getSegmentId(Integer count) {
        List<Long> ids = new ArrayList<>();
        try{
            if(count == null || count <= 0){
                count = 1;
            }
            long start = System.currentTimeMillis();
            for(int i=0; i<count; i++){
                long id = sequenceFacade.nextSegmentId("testSegmentSingleId");
                ids.add(id);
            }
            long cost = System.currentTimeMillis()-start;
            System.out.println("getSegmentId count="+count+",cost="+cost+"(ms)");
        }catch(Exception e){
            e.printStackTrace();
        }
        return Mono.justOrEmpty(ids);
    }

    @RequestMapping(value = "/listSegmentId")
    public Mono<List<Long>> listSegmentId(int count) {
        List<Long> idList = new ArrayList<>();
        try{
            long start = System.currentTimeMillis();
            idList = sequenceFacade.nextSegmentId("testSegmentBatchId", count);
            long cost = System.currentTimeMillis()-start;
            System.out.println("listSegmentId count="+count+",cost="+cost+"(ms)");
        }catch(Exception e){
            e.printStackTrace();
        }
        return Mono.just(idList);
    }

    @RequestMapping(value = "/snowPerformance")
    public Mono<String> snowPerformance() {
        int maxTotal = 1000000;

        try{
            int totalCount = 0, batchCount = 1000;
            long start = System.currentTimeMillis();
            for(; totalCount<maxTotal; ){
                List<Long> idList = sequenceFacade.nextSnowId(batchCount);
                totalCount += idList.size();
            }
            long cost = System.currentTimeMillis()-start;
            System.out.println("snowPerformanceBatch totalCount="+totalCount+",cost="+cost+"(ms)");
        }catch(Exception e){
            e.printStackTrace();
        }

//        try{
//            int totalCount = 0;
//            long start = System.currentTimeMillis();
//            for(; totalCount<maxTotal; ){
//                Long id = sequenceFacade.nextSnowId();
//                totalCount ++;
//            }
//            long cost = System.currentTimeMillis()-start;
//            System.out.println("snowPerformanceSingle totalCount="+totalCount+",cost="+cost+"(ms)");
//        }catch(Exception e){
//            e.printStackTrace();
//        }
        return Mono.just("ok");
    }

    @RequestMapping(value = "/redisPerformance")
    public Mono<String> redisPerformance() {
        int maxTotal = 1000000;

        try{
            int totalCount = 0, batchCount = 1000;
            long start = System.currentTimeMillis();
            for(; totalCount<maxTotal; ){
                List<Long> idList = sequenceFacade.nextRedisId("redisPerformanceBatch", batchCount);
                totalCount += idList.size();
            }
            long cost = System.currentTimeMillis()-start;
            System.out.println("redisPerformanceBatch totalCount="+totalCount+",cost="+cost+"(ms)");
        }catch(Exception e){
            e.printStackTrace();
        }

//        try{
//            int totalCount = 0;
//            long start = System.currentTimeMillis();
//            for(; totalCount<maxTotal; ){
//                Long id = sequenceFacade.nextRedisId("redisPerformanceSingle");
//                totalCount ++;
//            }
//            long cost = System.currentTimeMillis()-start;
//            System.out.println("redisPerformanceSingle count="+totalCount+",cost="+cost+"(ms)");
//        }catch(Exception e){
//            e.printStackTrace();
//        }
        return Mono.just("ok");
    }
}
