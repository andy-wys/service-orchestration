package org.andy.so.core;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import org.andy.so.core.entity.SoResp;
import org.andy.so.core.error.SoCheckErrorEnum;
import org.andy.so.core.error.SoCheckException;
import org.andy.so.core.schema.node.SoMerchantNode;
import org.andy.so.core.service.SoMerchantServiceHandler;
import org.andy.so.core.trace.SoTraceConstant;
import org.andy.so.core.trace.SoTraceHelper;
import org.andy.so.core.util.SoClassUtil;
import org.andy.so.core.util.SoStringUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * <h2>执行本地接口配置帮助类</h2>
 * 提供静态方法调用配置服务或 {@link SoApiService} 服务
 *
 * @author: andy
 */
@SuppressWarnings("unused,rawtypes,unchecked")
public class SoExecLocalApi {
    private static final Log log = LogFactory.getLog(SoExecLocalApi.class);
    private static SoMerchantFactory factory;
    private static SoMerchantServiceHandler soMerchantServiceHandler;

    /**
     * <h2>通过商户编码和接口名称查找并执行服务</h2>
     *
     * @param merchantCode 商户编码
     * @param apiPath      配置路径
     * @param param        请求参数
     * @return 标准返回数据对象
     */
    public static Object execute(String merchantCode, String apiPath, String param) {
        return execute(null, null, merchantCode, apiPath, param);
    }


    /**
     * <h2>通过商户编码和接口名称查找并执行服务</h2>
     *
     * @param merchantCode 商户编码
     * @param apiPath      配置路径
     * @param param        请求参数
     * @param tClass       执行结果返回类型
     * @param <T>          执行结果返回类型
     * @return 标准返回数据对象
     */
    public static <T> T execute(String merchantCode, String apiPath, String param, Class<T> tClass) {
        Object res = execute(null, null, merchantCode, apiPath, param);
        if (res == null) {
            return null;
        }
        if (res.getClass().getName().equals(tClass.getName())) {
            return (T) res;
        }
        return JSON.to(tClass, res);
    }


    /**
     * <h2>通过商户编码和接口名称查找并执行服务</h2>
     *
     * @param merchantCode  商户编码
     * @param apiPath       配置路径
     * @param param         请求参数
     * @param typeReference 类型转换
     * @param <T>           执行结果返回类型
     * @return 标准返回数据对象
     */
    public static <T> T execute(String merchantCode, String apiPath, String param, TypeReference<T> typeReference) {
        Object res = execute(null, null, merchantCode, apiPath, param);
        if (res == null) {
            return null;
        }
        if (res instanceof JSONObject) {
            return ((JSONObject) res).to(typeReference);
        }
        return JSON.parseObject(JSON.toJSONString(res), typeReference);
    }

    /**
     * <h2>通过商户编码和接口名称查找并执行服务</h2>
     *
     * @param request      HttpServletRequest
     * @param response     HttpServletResponse
     * @param merchantCode 商户编码
     * @param apiPath      接口路径
     * @param param        请求参数
     * @return 执行结果
     */
    public static Object execute(HttpServletRequest request,
                                 HttpServletResponse response,
                                 String merchantCode,
                                 String apiPath,
                                 String param) {
        if (findMerchantFactory() == null) {
            return SoResp.buildError(SoCheckErrorEnum.UNDEFINED_SERVICE_ERROR);
        }
        SoApiService apiService = factory.getApiService(merchantCode, apiPath);
        if (apiService != null) {
            return execute(apiService, request, response, param);
        }
        return executeXmlService(request, response, merchantCode, apiPath, param);
    }

    /**
     * <h2>通过商户编码和接口名称查找并执行 XML 配置服务</h2>
     *
     * @param request      HttpServletRequest
     * @param response     HttpServletResponse
     * @param merchantCode merchantCode
     * @param apiPath      apiPath
     * @param param        param
     * @return xml 配置执行结果
     */
    public static Object executeXmlService(HttpServletRequest request,
                                           HttpServletResponse response,
                                           String merchantCode,
                                           String apiPath,
                                           String param) {
        if (findHandleMerchantService() == null) {
            return SoResp.buildError(SoCheckErrorEnum.UNDEFINED_SERVICE_ERROR);
        }
        injectTraceId(request, response);
        Object result = soMerchantServiceHandler.handle(request, response, merchantCode, apiPath, param);
        removeTraceId();
        return result;
    }

    /**
     * <h2>调用自定义服务接口实现</h2>
     *
     * @param apiService 自定义服务类
     * @param request    请求
     * @param response   响应
     * @param param      请求参数
     * @return 响应报文
     */
    public static Object execute(SoApiService apiService, HttpServletRequest request, HttpServletResponse response, String param) {
        injectTraceId(request, response);
        final String methodName = "handle";
        Type type = SoClassUtil.getGenericType(apiService, 0);
        Method handleMethod = null;
        if (type != null) {
            try {
                handleMethod = apiService.getClass().getMethod(methodName, HttpServletRequest.class, HttpServletResponse.class, Class.forName(type.getTypeName()));
            } catch (NoSuchMethodException | ClassNotFoundException e) {
                log.error("调用自定义服务 handle 方法出现异常", e);
            }
        }
        if (handleMethod == null) {
            throw new SoCheckException(SoCheckErrorEnum.UNDEFINED_HANDLE_METHOD);
        }
        if (Object.class.getName().equals(type.getTypeName())) {
            return apiService.handle(request, response, param);
        }
        Object result = apiService.handle(request, response, JSON.parseObject(param, type));
        removeTraceId();
        return result;
    }

    /**
     * <h2>通过商户编码和接口名称查找配置服务</h2>
     *
     * @param merchantCode 商户编码
     * @param apiPath      接口地址
     * @return 接口配置
     */
    public static SoMerchantNode findApiConfig(String merchantCode, String apiPath) {
        if (findMerchantFactory() == null) {
            return null;
        }
        return factory.getMerchantNode(merchantCode, apiPath);
    }

    /**
     * <h2>通过商户编码和接口名称查找配置服务</h2>
     *
     * @param merchantCode 商户编码
     * @param apiPath      接口地址
     * @return 服务实现类
     */
    public static SoApiService findApiService(String merchantCode, String apiPath) {
        if (findMerchantFactory() == null) {
            return null;
        }
        return factory.getApiService(merchantCode, apiPath);
    }

    /**
     * <h2>初始化 {@link SoMerchantFactory}</h2>
     *
     * @return 未找到则返回 null
     */
    private static SoMerchantFactory findMerchantFactory() {
        if (factory != null) {
            return factory;
        }
        synchronized (SoExecLocalApi.class) {
            factory = SoApplicationContextAware.getBean(SoMerchantFactory.class);
            if (factory != null) {
                return factory;
            }
            Map<String, SoMerchantFactory> map = SoApplicationContextAware.getBeansOfType(SoMerchantFactory.class);
            if (!CollectionUtils.isEmpty(map.values())) {
                factory = map.values().stream().findFirst().orElse(null);
            }
        }
        return factory;
    }

    /**
     * <h2>初始化 {@link SoMerchantServiceHandler}</h2>
     *
     * @return 未找到对应的 spring bean 则返回 null
     */
    private static SoMerchantServiceHandler findHandleMerchantService() {
        if (soMerchantServiceHandler != null) {
            return soMerchantServiceHandler;
        }

        synchronized (SoExecLocalApi.class) {
            soMerchantServiceHandler = SoApplicationContextAware.getBean(SoMerchantServiceHandler.class);
            if (soMerchantServiceHandler != null) {
                return soMerchantServiceHandler;
            }
            Map<String, SoMerchantServiceHandler> map = SoApplicationContextAware.getBeansOfType(SoMerchantServiceHandler.class);
            if (!CollectionUtils.isEmpty(map.values())) {
                soMerchantServiceHandler = map.values().stream().findFirst().orElse(null);
            }
        }
        return soMerchantServiceHandler;
    }

    /**
     * 注入 trace id
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     */
    private static void injectTraceId(HttpServletRequest request, HttpServletResponse response) {
        String traceId = SoTraceHelper.get(SoTraceConstant.KEY_TRACE_ID);
        if (SoStringUtil.isNotBlank(traceId)) {
            return;
        }
        if (request != null) {
            traceId = request.getHeader(SoTraceConstant.KEY_TRACE_ID);
        }
        if (SoStringUtil.isBlank(traceId)) {
            traceId = SoTraceHelper.generateTraceId();
            log.debug("traceId 为空，重新生成：" + traceId);
        } else {
            log.debug("从 header 中取到了 traceId = " + traceId);
        }
        if (response != null) {
            response.setHeader(SoTraceConstant.KEY_TRACE_ID, traceId);
        }

        SoTraceHelper.put(SoTraceConstant.KEY_TRACE_ID, traceId);
        SoTraceHelper.put(SoTraceConstant.KEY_TRACE_OWNER, SoTraceConstant.TRACE_OWNER_SA);
    }

    /**
     * 删除 trace id
     */
    private static void removeTraceId() {
        String traceOwner = SoTraceHelper.get(SoTraceConstant.KEY_TRACE_OWNER);
        if (SoStringUtil.isNotBlank(traceOwner)) {
            SoTraceHelper.clear();
        }
    }
}
