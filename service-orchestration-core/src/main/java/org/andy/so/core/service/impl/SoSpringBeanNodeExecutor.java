package org.andy.so.core.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.andy.so.core.SoApplicationContextAware;
import org.andy.so.core.error.SoServiceErrorEnum;
import org.andy.so.core.error.SoServiceException;
import org.andy.so.core.schema.SoServiceNodeType;
import org.andy.so.core.schema.enums.SoPropNodeTypeEnum;
import org.andy.so.core.schema.enums.SoServiceNodeChildTypeEnum;
import org.andy.so.core.schema.enums.SoServiceNodeTypeEnum;
import org.andy.so.core.schema.node.SoPropertyNode;
import org.andy.so.core.schema.node.SoServiceNode;
import org.andy.so.core.service.SoExecNodeServiceData;
import org.andy.so.core.service.SoServiceNodeExecutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.reflect.Modifier.PUBLIC;

/**
 * <h2>spring 服务执行实现类</h2>
 *
 * @author: andy
 */
public class SoSpringBeanNodeExecutor implements SoServiceNodeExecutor<String> {
    private final Log log = LogFactory.getLog(getClass());

    @Override
    public boolean isMatchedExecutor(SoServiceNodeType serviceNodeType) {
        return SoServiceNodeTypeEnum.JSF == serviceNodeType
                || SoServiceNodeTypeEnum.LOCAL == serviceNodeType;
    }

    /**
     * JSF 调用
     *
     * @param apiConfig      节点配置
     * @param response       servlet
     * @param currentApiData 当前节点数据
     */
    @Override
    public String execute(SoServiceNode apiConfig, HttpServletRequest request, HttpServletResponse response, SoExecNodeServiceData currentApiData) {
        Object consumer = SoApplicationContextAware.getBean(apiConfig.getServiceId());
        if (consumer == null) {
            if (apiConfig.getNodeType() == SoServiceNodeTypeEnum.JSF) {
                throw new SoServiceException(SoServiceErrorEnum.NOT_FOUND_RPC_CONSUMER, null, apiConfig.getServiceId());
            } else {
                throw new SoServiceException(SoServiceErrorEnum.NOT_FOUND_SPRING_BEAN, null, apiConfig.getServiceId());
            }
        }
        Class<?> cls = consumer.getClass();
        // 先找到匹配的方法
        Method method = findMatchMethod(apiConfig, cls);
        Parameter[] methodParams = method.getParameters();
        try {
            // 无参直接调用
            if (methodParams == null || methodParams.length == 0) {
                Object result = method.invoke(cls);
                return JSON.toJSONString(result);
            }

            // 有参则构造参数，此时参数长度是相等的，所以可以直接用下标取值
            int len = methodParams.length;
            List<SoPropertyNode> paramConfList = findParamTypeList(apiConfig);
            LinkedList<Object> paramValues = new LinkedList<>();
            Object reqBody = currentApiData.getReqBody();
            String jsonValue;
            if (reqBody instanceof JSONObject) {
                jsonValue = ((JSONObject) reqBody).toJSONString();
            } else {
                jsonValue = JSON.toJSONString(reqBody);
            }

            int idx = 0;
            for (; idx < len; idx++) {
                paramValues.add(parseTypeValue(methodParams[idx].getType(), paramConfList.get(idx), jsonValue));
            }

            log.info("开始执行 [" + consumer.getClass().getName() + "#" + method.getName() + "]，请求数据: " + JSON.toJSONString(paramValues));
            Object result = method.invoke(consumer, paramValues.toArray());
            String resultStr = JSON.toJSONString(result);
            log.info("结束执行 [" + consumer.getClass().getName() + "#" + method.getName() + "]，请求数据: " + resultStr);
            if (JSON.isValid(resultStr)) {
                return resultStr;
            }
            return String.valueOf(result);
        } catch (IllegalAccessException | InvocationTargetException e) {
            if (apiConfig.getNodeType() == SoServiceNodeTypeEnum.JSF) {
                throw new SoServiceException(SoServiceErrorEnum.REMOTE_JSF_ERROR, e, apiConfig.getServiceId(), apiConfig.getMethod());
            } else {
                throw new SoServiceException(SoServiceErrorEnum.EXEC_BEAN_ERROR, e, apiConfig.getServiceId(), apiConfig.getMethod());
            }
        }
    }

    /**
     * 类型转换
     *
     * @param type        类型
     * @param node        节点
     * @param valueSource 数据源
     * @return 从数据源取值并转换数据类型
     */
    private Object parseTypeValue(Class<?> type, SoPropertyNode node, String valueSource) {
        Object value = JSONPath.extract(valueSource, node.getTargetKey());
        return JSON.to(type, value);
    }

    /**
     * 找到匹配的方法
     *
     * @param apiConfig 节点配置
     * @param cls       class
     * @return method
     */
    private Method findMatchMethod(SoServiceNode apiConfig, Class<?> cls) {
        // 先通过方法名和修饰符找到 method
        List<Method> methods = Arrays.stream(cls.getMethods())
                .filter(m -> m.getName().equals(apiConfig.getMethod()) && ((m.getModifiers() & PUBLIC) != 0) && !m.isBridge())
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(methods)) {
            if (apiConfig.getNodeType() == SoServiceNodeTypeEnum.JSF) {
                throw new SoServiceException(SoServiceErrorEnum.NOT_FOUND_RPC_METHOD, null, apiConfig.getServiceId(), apiConfig.getMethod());
            } else {
                throw new SoServiceException(SoServiceErrorEnum.NOT_FOUND_BEAN_METHOD, null, apiConfig.getServiceId(), apiConfig.getMethod());
            }
        }

        List<SoPropertyNode> paramTypeList = findParamTypeList(apiConfig);

        // 多个方法按类型匹配
        Class<?>[] paramTypes;
        int paramSize = paramTypeList.size();
        for (Method m : methods) {
            if (paramSize != m.getParameterCount()) {
                continue;
            }
            paramTypes = m.getParameterTypes();
            boolean isMathched = true;
            for (int i = 0; i < paramSize; i++) {
                if (paramTypes[i].getName().equalsIgnoreCase(paramTypeList.get(i).getDataType())
                        || paramTypes[i].getSimpleName().equalsIgnoreCase(paramTypeList.get(i).getDataType())) {
                    continue;
                }
                isMathched = false;
                break;
            }
            if (isMathched) {
                return m;
            }
        }
        if (apiConfig.getNodeType() == SoServiceNodeTypeEnum.JSF) {
            throw new SoServiceException(SoServiceErrorEnum.NOT_FOUND_RPC_METHOD, null, apiConfig.getServiceId(), apiConfig.getMethod());
        } else {
            throw new SoServiceException(SoServiceErrorEnum.NOT_FOUND_BEAN_METHOD, null, apiConfig.getServiceId(), apiConfig.getMethod());
        }
    }

    /**
     * 筛选出 param 类型配置
     *
     * @param apiConfig 节点配置
     * @return 参数配置
     */
    private List<SoPropertyNode> findParamTypeList(SoServiceNode apiConfig) {
        LinkedList<SoPropertyNode> paramTypeList = new LinkedList<>();
        List<SoPropertyNode> propertyNodeList = apiConfig.getPropMap().get(SoServiceNodeChildTypeEnum.REQ.name());
        if (!CollectionUtils.isEmpty(propertyNodeList)) {
            propertyNodeList.forEach(node -> {
                if (node.getPropNodeType() == SoPropNodeTypeEnum.PARAM) {
                    paramTypeList.add(node);
                }
            });
        }
        return paramTypeList;
    }
}
