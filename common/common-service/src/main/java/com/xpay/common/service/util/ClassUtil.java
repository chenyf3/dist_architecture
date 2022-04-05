package com.xpay.common.service.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author chenyf
 */
public class ClassUtil {
    /**
     * The package separator character: {@code '&#x2e;' == {@value}}.
     */
    public static final char PACKAGE_SEPARATOR_CHAR = '.';
    /**
     * The inner class separator character: {@code '$' == {@value}}.
     */
    public static final char INNER_CLASS_SEPARATOR_CHAR = '$';

    private static final Map<String, Class<?>> namePrimitiveMap = new HashMap();
    private static final Map<Class<?>, Class<?>> primitiveWrapperMap;
    private static final Map<String, String> abbreviationMap;
    private static ClassLoader classLoader;

    static {
        classLoader = Thread.currentThread().getContextClassLoader();

        namePrimitiveMap.put("boolean", Boolean.TYPE);
        namePrimitiveMap.put("byte", Byte.TYPE);
        namePrimitiveMap.put("char", Character.TYPE);
        namePrimitiveMap.put("short", Short.TYPE);
        namePrimitiveMap.put("int", Integer.TYPE);
        namePrimitiveMap.put("long", Long.TYPE);
        namePrimitiveMap.put("double", Double.TYPE);
        namePrimitiveMap.put("float", Float.TYPE);
        namePrimitiveMap.put("void", Void.TYPE);
        primitiveWrapperMap = new HashMap();
        primitiveWrapperMap.put(Boolean.TYPE, Boolean.class);
        primitiveWrapperMap.put(Byte.TYPE, Byte.class);
        primitiveWrapperMap.put(Character.TYPE, Character.class);
        primitiveWrapperMap.put(Short.TYPE, Short.class);
        primitiveWrapperMap.put(Integer.TYPE, Integer.class);
        primitiveWrapperMap.put(Long.TYPE, Long.class);
        primitiveWrapperMap.put(Double.TYPE, Double.class);
        primitiveWrapperMap.put(Float.TYPE, Float.class);
        primitiveWrapperMap.put(Void.TYPE, Void.TYPE);
        Iterator var0 = primitiveWrapperMap.entrySet().iterator();

        while(var0.hasNext()) {
            Map.Entry<Class<?>, Class<?>> entry = (Map.Entry)var0.next();
            Class<?> primitiveClass = (Class)entry.getKey();
            Class<?> wrapperClass = (Class)entry.getValue();
            if (!primitiveClass.equals(wrapperClass)) {
            }
        }

        Map<String, String> m = new HashMap();
        m.put("int", "I");
        m.put("boolean", "Z");
        m.put("float", "F");
        m.put("long", "J");
        m.put("short", "S");
        m.put("byte", "B");
        m.put("double", "D");
        m.put("char", "C");
        Map<String, String> r = new HashMap();
        Iterator var6 = m.entrySet().iterator();

        while(var6.hasNext()) {
            Map.Entry<String, String> e = (Map.Entry)var6.next();
            r.put(e.getValue(), e.getKey());
        }

        abbreviationMap = Collections.unmodifiableMap(m);
    }

    /**
     * 通过反射, 获得定义Class时声明的父类的泛型参数的类型. 如无法找到, 返回Object.class.
     *
     * @param clazz The class to introspect
     * @param index the Index of the generic declaration,start from 0.
     * @return the index generic declaration, or Object.class if cannot be
     * determined
     */
    public static <T> Class<Object> getSuperClassGenericType(final Class<T> clazz, final int index) {
        //返回表示此 Class 所表示的实体（类、接口、基本类型、void）的直接超类的 Type。
        Type genType = clazz.getGenericSuperclass();

        if (! (genType instanceof ParameterizedType)) {
            return Object.class;
        }

        //返回表示此类型实际类型参数的 Type 对象的数组。
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        if (index >= params.length || index < 0) {
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            return Object.class;
        }

        return (Class) params[index];
    }

    public static Class<?> getClass(String className) throws ClassNotFoundException {
        return getClass(classLoader, className);
    }

    public static Class<?> getClass(ClassLoader classLoader, String className) throws ClassNotFoundException {
        return getClass(classLoader, className, true);
    }

    /**
     * Returns the class represented by {@code className} using the
     * {@code classLoader}.  This implementation supports the syntaxes
     * "{@code java.util.Map.Entry[]}", "{@code java.util.Map$Entry[]}",
     * "{@code [Ljava.util.Map.Entry;}", and "{@code [Ljava.util.Map$Entry;}".
     *
     * @param classLoader  the class loader to use to load the class
     * @param className  the class name
     * @param initialize  whether the class must be initialized
     * @return the class represented by {@code className} using the {@code classLoader}
     * @throws ClassNotFoundException if the class is not found
     */
    public static Class<?> getClass(
            final ClassLoader classLoader, final String className, final boolean initialize) throws ClassNotFoundException {
        try {
            Class<?> clazz;
            if (namePrimitiveMap.containsKey(className)) {
                clazz = namePrimitiveMap.get(className);
            } else {
                clazz = Class.forName(toCanonicalName(className), initialize, classLoader);
            }
            return clazz;
        } catch (final ClassNotFoundException ex) {
            // allow path separators (.) as inner class name separators
            final int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);

            if (lastDotIndex != -1) {
                try {
                    return getClass(classLoader, className.substring(0, lastDotIndex) +
                                    INNER_CLASS_SEPARATOR_CHAR + className.substring(lastDotIndex + 1),
                            initialize);
                } catch (final ClassNotFoundException ex2) { // NOPMD
                    // ignore exception
                }
            }

            throw ex;
        }
    }

    /**
     * Converts a class name to a JLS style class name.
     *
     * @param className  the class name
     * @return the converted name
     */
    private static String toCanonicalName(String className) {
        className = StringUtil.deleteWhitespace(className);
        assert className != null;
        if (className.endsWith("[]")) {
            final StringBuilder classNameBuffer = new StringBuilder();
            while (className.endsWith("[]")) {
                className = className.substring(0, className.length() - 2);
                classNameBuffer.append("[");
            }
            final String abbreviation = abbreviationMap.get(className);
            if (abbreviation != null) {
                classNameBuffer.append(abbreviation);
            } else {
                classNameBuffer.append("L").append(className).append(";");
            }
            className = classNameBuffer.toString();
        }
        return className;
    }
}
