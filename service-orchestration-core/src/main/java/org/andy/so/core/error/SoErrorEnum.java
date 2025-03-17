package org.andy.so.core.error;

import org.andy.so.core.SoError;

/**
 * <h3>网关公共错误类型定义，用来替代 CommonErrorEnum</h3>
 * 9000 - 9999 为公共类型，请勿占用
 *
 * @author: andy
 */
public enum SoErrorEnum implements SoError {
    SUCCESS("0000", "请求成功"),
    UNKNOWN("9999", "未知系统异常"),
    REQ_PARAM_ERROR("9998", "请求参数错误"),

    NOT_LOG_IN("9100", "用户未登录"),
    INVALID_LOGIN_STATUS_ORG("9101", "机构登录态失效"),
    INVALID_LOGIN_STATUS_USER("9102", "用户登录态失效"),
    UNAUTHORIZED("9103", "未授权当前操作"),
    ;

    private final String code;
    private final String message;

    SoErrorEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}

