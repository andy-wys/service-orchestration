package org.andy.so.core.entity;

import org.andy.so.core.SoError;
import org.andy.so.core.error.SoErrorEnum;

/**
 * <h2>标准出参报文</h2>
 *
 * @author: andy
 */
@SuppressWarnings("rawtypes,unused")
public class SoResp<T> {
    /**
     * <h2>响应编码</h2>
     */
    protected String code;
    /**
     * <h2>执行信息</h2>
     */
    protected String message;
    /**
     * <h2>执行结果</h2>
     */
    protected T data;

    /**
     * <h2>构建响应报文</h2>
     *
     * @param data 报文数据
     * @param <T>  data 数据类型
     * @return resp
     */
    public static <T> SoResp<T> build(T data) {
        if (data instanceof SoError) {
            build(((SoError) data).getCode(), ((SoError) data).getMessage(), null);
        }
        return build(SoErrorEnum.SUCCESS, data);
    }

    /**
     * <h2>构建响应报文</h2>
     *
     * @param error 错误类型定义
     * @param data  报文数据
     * @param <T>   data 数据类型
     * @return resp
     */
    public static <T> SoResp<T> build(SoError error, T data) {
        return build(error.getCode(), error.getMessage(), data);
    }

    /**
     * <h2>构建响应报文</h2>
     *
     * @param error 错误类型定义
     * @return resp
     */
    public static SoResp buildError(SoError error) {
        return build(error.getCode(), error.getMessage(), null);
    }

    /**
     * <h2>构建响应报文</h2>
     *
     * @param code    请求状态码
     * @param message 请求状态信息
     * @param data    报文数据
     * @param <T>     data 数据类型
     * @return resp
     */
    public static <T> SoResp<T> build(String code, String message, T data) {
        SoResp<T> resp = new SoResp<>();
        resp.setCode(code);
        resp.setMessage(message);
        resp.setData(data);
        return resp;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
