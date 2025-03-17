package org.andy.so.core.entity;

import java.io.Serializable;

/**
 * <h2>标准接口请求报文</h2>
 *
 * @author: andy
 */
@SuppressWarnings("unused")
public class SoReq implements Serializable {
    private static final long serialVersionUID = 3305524337360559163L;
    /**
     * <h2>登录后的认证信息</h2>
     */
    protected String token;
    /**
     * <h2>请求必传，商户编码</h2>
     */
    protected String merchantCode;
    /**
     * <h2>接口地址</h2>
     */
    protected String apiPath;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMerchantCode() {
        return merchantCode;
    }

    public void setMerchantCode(String merchantCode) {
        this.merchantCode = merchantCode;
    }

    public String getApiPath() {
        return apiPath;
    }

    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }
}
