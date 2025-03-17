package org.andy.so.core.help.impl;

import com.alibaba.fastjson2.JSON;
import org.andy.so.core.SoApplicationContextAware;
import org.andy.so.core.SoDataHandle;
import org.andy.so.core.error.SoCheckErrorEnum;
import org.andy.so.core.error.SoCheckException;
import org.andy.so.core.extend.*;
import org.andy.so.core.help.SoDataHandleHelper;
import org.andy.so.core.schema.SoSchemaConstant;
import org.andy.so.core.schema.enums.SoConditionTypeEnum;
import org.andy.so.core.util.SoClassUtil;
import org.andy.so.core.util.SoStringUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * <h2>数据处理器工厂类，用于管理和执行处理器</h2>
 *
 * @author: andy
 */
public class SoDataHandleHelperImpl implements SoDataHandleHelper {
    private final Log log = LogFactory.getLog(getClass());
    /**
     * <h3>用于保存数据转换的处理类</h3>
     */
    final Map<String, SoDataHandle<?, ?>> handleMap = new HashMap<>();

    public SoDataHandleHelperImpl() {
        initDataConvertHandle();
    }

    /**
     * <h3>初始化数据处理器</h3>
     */
    @SuppressWarnings("rawtypes")
    public void initDataConvertHandle() {
        // 扫描 SoDataConvertHandle 接口的实现类
        Map<String, SoDataHandle> convertImpls = SoApplicationContextAware.getBeansOfType(SoDataHandle.class);

        convertImpls.forEach((k, v) -> {
            addDataConvertHandle(v.getHandleName(), v);
            addDataConvertHandle(k, v);
            addDataConvertHandle(v.getClass().getName(), v);
        });
        // 预设类型：比较
        addDataConvertHandle(new SoCompareDataHandle(SoConditionTypeEnum.EQUAL));
        addDataConvertHandle(new SoCompareDataHandle(SoConditionTypeEnum.GREATER_THAN));
        addDataConvertHandle(new SoCompareDataHandle(SoConditionTypeEnum.GREATER_EQUAL));
        addDataConvertHandle(new SoCompareDataHandle(SoConditionTypeEnum.LESS_THAN));
        addDataConvertHandle(new SoCompareDataHandle(SoConditionTypeEnum.LESS_EQUAL));
        addDataConvertHandle(new SoEqualsIgnoreCaseHandle());
        addDataConvertHandle(new SoInDataHandle());
        addDataConvertHandle(new SoNotInDataHandle());
        addDataConvertHandle(new SoBetweenDataHandle());

        // 预设类型：数据转换
        addDataConvertHandle(new SoParseDataTypeHandle(SoParseDataTypeHandle.ParseType.TO_NUMBER));
        addDataConvertHandle(new SoParseDataTypeHandle(SoParseDataTypeHandle.ParseType.TO_STRING));
        addDataConvertHandle(new SoParseDataTypeHandle(SoParseDataTypeHandle.ParseType.TO_BOOLEAN));
        addDataConvertHandle(new SoParseDataTypeHandle(SoParseDataTypeHandle.ParseType.TO_INTEGER));
        addDataConvertHandle(new SoParseDataTypeHandle(SoParseDataTypeHandle.ParseType.TO_INTEGER_FLOOR));
        addDataConvertHandle(new SoParseDataTypeHandle(SoParseDataTypeHandle.ParseType.TO_INTEGER_CEIL));
        addDataConvertHandle(new SoParseDataTypeHandle(SoParseDataTypeHandle.ParseType.TO_INTEGER_ROUND));
        addDataConvertHandle(new SoParseDataTypeHandle(SoParseDataTypeHandle.ParseType.TO_MONEY));
        addDataConvertHandle(new SoParseDataTypeHandle(SoParseDataTypeHandle.ParseType.TO_MONEY_FLOOR));
        addDataConvertHandle(new SoParseDataTypeHandle(SoParseDataTypeHandle.ParseType.TO_MONEY_CEIL));
        addDataConvertHandle(new SoParseDataTypeHandle(SoParseDataTypeHandle.ParseType.TO_MONEY_ROUND));
        addDataConvertHandle(new SoParseDataTypeHandle(SoParseDataTypeHandle.ParseType.TO_DATE));
        addDataConvertHandle(new SoParseDataTypeHandle(SoParseDataTypeHandle.ParseType.TO_DATETIME));
        addDataConvertHandle(new SoParseDataTypeHandle(SoParseDataTypeHandle.ParseType.TO_YYYY_MM_DD));
        addDataConvertHandle(new SoParseDataTypeHandle(SoParseDataTypeHandle.ParseType.TO_YYYYMMDD));
        addDataConvertHandle(new SoParseDataTypeHandle(SoParseDataTypeHandle.ParseType.TO_YYYY_MM_DD_HH_MM_SS));
        addDataConvertHandle(new SoParseDataTypeHandle(SoParseDataTypeHandle.ParseType.TO_YYYYMMDD_HHMMSS));
        addDataConvertHandle(new SoParseDataTypeHandle(SoParseDataTypeHandle.ParseType.TO_YYYYMMDDHHMMSS));
        addDataConvertHandle(new SoParseDataTypeHandle(SoParseDataTypeHandle.ParseType.TO_HHMMSS));
        addDataConvertHandle(new SoParseDataTypeHandle(SoParseDataTypeHandle.ParseType.TO_HH_MM_SS));
    }

    /**
     * <h3>将数据处理器添加到 {@code handleMap} 中</h3>
     * map: type -> instance
     *
     * @param type     类型名称，不能为空，否则不添加
     * @param instance 处理器，不能为空，否则不添加
     */
    @Override
    public void addDataConvertHandle(String type, SoDataHandle<?, ?> instance) {
        if (SoStringUtil.isEmpty(type) || instance == null) {
            return;
        }
        if (handleMap.containsKey(type.trim())) {
            throw new SoCheckException(SoCheckErrorEnum.DATA_HANDLE_HAS_EXIST, null, type);
        }
        handleMap.put(type.trim(), instance);
    }

    /**
     * <h3>将数据处理器添加到 {@code handleMap} 中，key 为 {@code instance#getHandleName()}</h3>
     *
     * @param instance 处理器实现
     */
    @Override
    public void addDataConvertHandle(SoDataHandle<?, ?> instance) {
        if (instance == null || SoStringUtil.isBlank(instance.getHandleName())) {
            return;
        }
        String handleName = instance.getHandleName().trim();
        SoDataHandle<?, ?> handle = handleMap.get(handleName);
        if (handle == null) {
            handleMap.put(handleName, instance);
            return;
        }
        if (handle == instance) {
            return;
        }
        throw new SoCheckException(SoCheckErrorEnum.DATA_HANDLE_HAS_EXIST, null, handleName);
    }

    /**
     * <h3>找到 type 对应的处理类，然后执行 doConvert 方法进行数据转换</h3>
     *
     * @param type       转换类型
     * @param sourceData 源数据
     * @param params     参数
     * @return 转换后的数据
     */
    @Override
    public Object doConvert(String type, Object sourceData, Object... params) {
        if (SoStringUtil.isBlank(type)) {
            return sourceData;
        }
        String[] typeArr = type.split(SoSchemaConstant.SPLIT_CHAR_CONFIG);
        SoDataHandle<?, ?> handle;
        Object targetData = sourceData;
        for (String s : typeArr) {
            handle = handleMap.get(s.trim());
            if (handle == null) {
                break;
            }
            log.info("开始执行数据处理器[" + s + "]，处理前数据：" + JSON.toJSONString(targetData));
            Method method = findDoConvertMethod(handle);
            try {
                if (targetData == null) {
                    targetData = handle.doConvert(null, params);
                } else if (method.getParameterTypes()[0].isAssignableFrom(targetData.getClass())) {
                    targetData = method.invoke(handle, targetData, params);
                } else if (targetData instanceof String) {
                    targetData = method.invoke(handle, JSON.parseObject((String) targetData, method.getParameterTypes()[0]), params);
                } else {
                    targetData = method.invoke(handle, JSON.parseObject(JSON.toJSONString(targetData), method.getParameterTypes()[0]), params);
                }
                log.info("执行结束数据处理器[" + s + "]，处理后数据：" + JSON.toJSONString(targetData));
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error("执行结束数据处理器 [" + s + "] 出现异常: ", e);
            }
        }
        return targetData;
    }

    /**
     * 找到 SoDataHandle#doConvert 方法
     *
     * @param handle data handle
     * @return doConvert(data, params)
     */
    private Method findDoConvertMethod(SoDataHandle<?, ?> handle) {
        String convertMethodName = "doConvert";
        Type genericType = SoClassUtil.getGenericType(handle, 0);
        Method method = null;
        if (genericType != null) {
            try {
                method = handle.getClass().getMethod(
                        convertMethodName,
                        Class.forName(genericType.getTypeName()),
                        Object[].class);
            } catch (NoSuchMethodException | ClassNotFoundException e) {
                log.error("在查找 doConvert 方法时出现异常", e);
            }
        }
        if (method != null) {
            return method;
        }

        // 先从自己声明的方法中找
        method = Arrays.stream(handle.getClass().getDeclaredMethods())
                .filter(m -> convertMethodName.equals(m.getName()) && ((m.getModifiers() & Modifier.PUBLIC) != 0) && !m.isBridge())
                .findFirst()
                .orElse(null);
        if (method == null) {
            method = Arrays.stream(handle.getClass().getMethods())
                    .filter(m -> convertMethodName.equals(m.getName()) && ((m.getModifiers() & Modifier.PUBLIC) != 0) && !m.isBridge())
                    .findFirst()
                    .orElse(null);
        }

        if (method == null) {
            throw new SoCheckException(SoCheckErrorEnum.UNDEFINED_DO_CONVERT_METHOD, null, handle.getClass().getName());
        }
        return method;
    }
}
