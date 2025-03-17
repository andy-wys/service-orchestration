package org.andy.so.core;

import com.alibaba.fastjson2.JSONObject;
import org.andy.so.core.anno.SoApiController;
import org.andy.so.core.anno.SoApiRegister;
import org.andy.so.core.anno.SoPost;
import org.andy.so.core.entity.SoResp;
import org.andy.so.core.error.SoCheckErrorEnum;
import org.andy.so.core.schema.node.SoMerchantNode;
import org.andy.so.core.util.SoJsonUtil;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <h2>定义服务统一请求入口，请求路径：{so.merchant.api-root-path:}/{merchantCode}/{apiPath}</h2>
 * <pre>1. 使用框架规范定义的 xml 配置接口</pre>
 * <pre>2. 实现 {@link  SoApiService} 接口并用 {@link SoApiRegister} 注解的 java 类</pre>
 *
 * @author: andy
 */
@SoApiController
public class SoApiCommonHandler {

    @Resource
    SoMerchantProperty merchantProperty;

    /**
     * <h2>暴露的请求接口，该方式已过时，请使用  {@link SoApiCommonHandler#handleMerchantApiPath} 代替</h2>
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @param param    请求参数
     * @param apiPath  接口名称
     * @return 响应报文
     */
    @SoPost(path = "${so.merchant.api-root-path:}/{apiPath}")
    @Deprecated
    public Object handleApiPath(HttpServletRequest request, HttpServletResponse response, @RequestBody JSONObject param, @PathVariable String apiPath) {
        if (param == null) {
            param = new JSONObject();
        }
        String merchantCode = SoJsonUtil.getString(param, "merchantCode");
        if (StringUtils.isEmpty(merchantCode)) {
            merchantCode = merchantProperty.getCode();
        }
        return handleMerchantApiPath(request, response, param.toJSONString(), merchantCode, apiPath);
    }

    /**
     * <h2>暴露统一的请求接口地址：{so.merchant.api-root-path:}/{merchantCode}/{apiPath}</h2>
     *
     * @param request      HttpServletRequest
     * @param response     HttpServletResponse
     * @param param        请求参数
     * @param merchantCode 商户编码
     * @param apiPath      接口名称
     * @return 响应报文
     * @since 1.2.0
     */
    @SoPost(path = "${so.merchant.api-root-path:}/{merchantCode}/{apiPath}")
    public Object handleMerchantApiPath(HttpServletRequest request, HttpServletResponse response,
                                        @RequestBody String param,
                                        @PathVariable String merchantCode,
                                        @PathVariable String apiPath) {

        SoApiService<?, ?> apiService = SoExecLocalApi.findApiService(merchantCode, apiPath);
        if (apiService != null) {
            SoApiRegister serviceRegAnno = apiService.getClass().getAnnotation(SoApiRegister.class);
            if (serviceRegAnno != null && serviceRegAnno.isRegRequestMapping()) {
                return SoExecLocalApi.execute(apiService, request, response, param);
            }
        }

        SoMerchantNode merchantNode = SoExecLocalApi.findApiConfig(merchantCode, apiPath);
        if (merchantNode != null && merchantNode.isRegRequestMapping()) {
            return SoExecLocalApi.executeXmlService(request, response, merchantCode, apiPath, param);
        }
        return SoResp.build(SoCheckErrorEnum.UNREGISTER_SERVICE_ERROR.getCode(),
                SoCheckErrorEnum.UNREGISTER_SERVICE_ERROR.getMessage() + ": code = " + merchantCode + ", apiPath = " + apiPath,
                null);
    }
}
