package org.andy.so.core.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * <h2>该工具类仅供当前框架使用</h2>
 *
 * @author: andy
 */
@SuppressWarnings("unused")
public final class SoClassUtil {
    private static final Map<String, Class<?>> NUMBER_CLASS_MAP = new HashMap<>();
    private static final Map<String, Class<?>> STRING_CLASS_MAP = new HashMap<>();
    private static final Map<String, Class<?>> BOOLEAN_CLASS_MAP = new HashMap<>();

    static {
        List<Class<?>> numberClass = Arrays.asList(
                Byte.class, Integer.class, Double.class,
                Float.class, Long.class, Short.class, BigDecimal.class);

        for (Class<?> clazz : numberClass) {
            NUMBER_CLASS_MAP.put(clazz.getName().toLowerCase(), clazz);
            NUMBER_CLASS_MAP.put(clazz.getSimpleName().toLowerCase(), clazz);
        }
        NUMBER_CLASS_MAP.put("int", Integer.class);

        List<Class<?>> stringClass = Arrays.asList(String.class, Character.class);
        for (Class<?> clazz : stringClass) {
            STRING_CLASS_MAP.put(clazz.getName().toLowerCase(), clazz);
            STRING_CLASS_MAP.put(clazz.getSimpleName().toLowerCase(), clazz);
        }

        BOOLEAN_CLASS_MAP.put(Boolean.class.getSimpleName().toLowerCase(), Boolean.class);
        BOOLEAN_CLASS_MAP.put(Boolean.class.getName().toLowerCase(), Boolean.class);
        BOOLEAN_CLASS_MAP.put("bool", Boolean.class);
    }

    /**
     * <h3>通过名称获取类型，这里包含了 java 基本类型的简写方式</h3>
     *
     * @param className 类名，可以是简写
     * @return Class
     */
    public static Class<?> getClassType(String className) {
        if (SoStringUtil.isBlank(className)) {
            return null;
        }
        Class<?> clazz = STRING_CLASS_MAP.get(className.toLowerCase());
        if (clazz != null) {
            return clazz;
        }

        clazz = NUMBER_CLASS_MAP.get(className.toLowerCase());
        if (clazz != null) {
            return clazz;
        }

        clazz = BOOLEAN_CLASS_MAP.get(className.toLowerCase());
        if (clazz != null) {
            return clazz;
        }

        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ignored) {
        }
        return null;
    }

    /**
     * <h3>判断是否为数字类型</h3>
     *
     * @param clazz Class
     * @return true 数字类型
     */
    public static boolean isNumberType(Class<?> clazz) {
        if (null == clazz) {
            return false;
        }
        return NUMBER_CLASS_MAP.containsKey(clazz.getName().toLowerCase());
    }

    /**
     * <h3>判断字符串是否为数字、小数、科学计数法</h3>
     *
     * @param str 判断的字符串
     * @return TRUE是数字
     */
    public static boolean isNumeric(String str) {
        if (null == str || str.isEmpty()) {
            return false;
        }
        String regx = "[+-]*\\d+\\.?\\d*[Ee]*[+-]*\\d+";
        Pattern pattern = Pattern.compile(regx);
        boolean isNumber = pattern.matcher(str).matches();
        if (isNumber) {
            return true;
        }
        regx = "^[-+]?[.\\d]*$";
        pattern = Pattern.compile(regx);
        return pattern.matcher(str).matches();
    }

    /**
     * <h3>判断对象是否为空或空字符串</h3>
     *
     * @param obj 校验对象
     * @return true则为空
     */
    public static boolean isBlack(Object obj) {
        if (obj == null) {
            return true;
        }
        return obj.toString().trim().isEmpty();
    }

    /**
     * <h3>获取指定对象的第 index 个泛型类型</h3>
     *
     * @param obj   要查找的实例对象
     * @param index 第 index 个泛型，index 从 0 开始
     * @return 泛型的实际类型
     */
    public static Type getGenericType(Object obj, int index) {
        Type type = obj.getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type[] actualTypeArr = ((ParameterizedType) type).getActualTypeArguments();
            if (actualTypeArr.length <= index) {
                return null;
            }
            return actualTypeArr[index];
        }
        return null;
    }
}
