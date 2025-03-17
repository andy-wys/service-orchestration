package org.andy.so.core.error;

import org.andy.so.core.SoError;

/**
 * <h3>服务执行错误定义</h3>
 *
 * @author: andy
 */
public enum SoServiceErrorEnum implements SoError {
    NOT_FOUND_RPC_CONSUMER("0030", "未找到 consumer [id=%1$s] 配置，请检查 RPC 配置是否正确"),
    NOT_FOUND_SPRING_BEAN("0031", "未找到 bean [id=%1$s] 配置，请检查 Bean 注入配置是否正确"),
    NOT_FOUND_RPC_METHOD("0032", "未找到 consumer [id=%1$s] 的方法[%2$s]，请检查 RPC 配置是否正确"),
    NOT_FOUND_BEAN_METHOD("0033", "未找到 bean [id=%1$s] 的方法[%2$s]，请检查 Bean 注入配置是否正确"),

    REMOTE_JSF_ERROR("0034", "调用 RPC 服务 [id=%1$s # %2$s] 异常"),
    EXEC_BEAN_ERROR("0035", "执行 bean [id=%1$s # %2$s] 异常"),
    NOT_FOUND_EXEC_NODE_SERVICE("0036", "未找到节点 [%1$s] 对应的执行服务，请检查配置或手动注册"),

    NOT_FOUND_REF_ERROR("0040", "未找到 REF 配置引用，请检查..."),

    REMOTE_REQUEST_ERROR("8000", "服务调用异常"),
    REMOTE_REDIRECT_URL("8001", "重定向远程地址错误，请检查配置是否正确"),
    REMOTE_REDIRECT_ERROR("8002", "重定向错误，请检查配置是否正确"),
    ;

    private final String code;
    private final String message;

    SoServiceErrorEnum(String code, String message) {
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
