package com.xpay.web.api.common.service.impl;

import com.xpay.common.statics.annotations.DictIgnore;
import com.xpay.web.api.common.ddo.vo.DictionaryVo;
import com.xpay.web.api.common.service.DictionaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultDictionaryService implements DictionaryService {
    public final static String STATIC_SCAN_PACKAGE_NAME = "com.xpay.common.statics.enums";
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Map<String, List<DictionaryVo.Item>> listAllDictionary() {
        Map<String, List<DictionaryVo.Item>> dictionaryMap = new HashMap<>();
        getStaticDictionaryMap(dictionaryMap);
        return dictionaryMap;
    }

    private void getStaticDictionaryMap(Map<String, List<DictionaryVo.Item>> dictionaryMap){
        List<String> enumNameList = getStaticEnumClassName();

        for(String className : enumNameList){
            if(dictionaryMap.containsKey(className+"")){//已经存在的则直接跳过
                continue;
            }

            Class<Enum> clazz;
            try{
                clazz = (Class<Enum>) Class.forName(className);
                if(clazz.getAnnotation(DictIgnore.class) != null){
                    continue;
                }
            }catch(Exception e){
                continue;
            }

            //根据方法名获取方法
            Method getValue = null;
            Method getDesc = null;
            try{
                getValue = clazz.getMethod("getValue");
            }catch(NoSuchMethodException | SecurityException e){
            }
            try{
                getDesc = clazz.getMethod("getDesc");
            }catch(NoSuchMethodException | SecurityException e){
            }

            List<DictionaryVo.Item> itemList = new ArrayList<>();
            Enum[] enumConstants = clazz.getEnumConstants();//获取所有枚举实例
            for (Enum enumConst : enumConstants) {
                DictionaryVo.Item item = new DictionaryVo.Item();

                Object value = null;
                Object desc = null;
                try{
                    value = getValue != null ? getValue.invoke(enumConst) : enumConst.name();
                }catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
                }

                try{
                    desc = getDesc != null ? getDesc.invoke(enumConst) : null;
                }catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
                }

                item.setName(enumConst.name());
                item.setCode(value != null ? value.toString() : null);
                item.setDesc(desc != null ? desc.toString() : null);
                itemList.add(item);
            }
            String simpleClassName = className.substring(className.lastIndexOf(".")+1);//只截取类名，不需要类的全限定名称
            dictionaryMap.put(simpleClassName, itemList);
        }
    }

    /**
     * 扫描指定包名下的所有类
     * @return
     */
    private List<String> getStaticEnumClassName(){
        final String enumPackageName = SystemPropertyUtils.resolvePlaceholders(STATIC_SCAN_PACKAGE_NAME);
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metaReader = new CachingMetadataReaderFactory(resolver);

        try{
            String packageSearchPath = "classpath*:" + ClassUtils.convertClassNameToResourcePath(enumPackageName) + "/**/*.class";

            Resource[] resources = resolver.getResources(packageSearchPath);

            List<String> classList = new ArrayList<>();
            for (Resource r : resources) {
                MetadataReader reader = metaReader.getMetadataReader(r);
                classList.add(reader.getClassMetadata().getClassName());
            }
            return classList;
        }catch(Exception e){
            logger.error("进行枚举类的包扫描时出现异常", e);
            return new ArrayList<>();
        }
    }
}
