package org.andy.so.core.error;

import org.andy.so.core.SoError;

import java.util.MissingFormatArgumentException;

/**
 * <h2>业务异常类</h2>
 *
 * @author: andy
 */
public class SoBizException extends RuntimeException implements SoError {
    private static final long serialVersionUID = 13367560744064466L;
    protected String errorCode;
    protected String errorMsg;

    /**
     * <h2>默认创建一个"未知系统异常"，错误码 9999</h2>
     */
    @SuppressWarnings("unused")
    public SoBizException() {
        this(SoErrorEnum.UNKNOWN);
    }

    /**
     * <h2>通过 {@link SoError} 构建一个业务异常类</h2>
     *
     * @param error 错误信息
     */
    public SoBizException(SoError error) {
        this(error, null);
    }

    /**
     * <h2>通过 {@link SoError} 和 {@link Throwable} 构建一个业务异常类</h2>
     *
     * @param error     错误信息
     * @param ex        原始异常类
     * @param formatMsg {@link SoError#getMessage()}数据格式化参数
     */
    public SoBizException(SoError error, Throwable ex, Object... formatMsg) {
        this(
                error == null ? SoErrorEnum.UNKNOWN.getCode() : error.getCode(),
                error == null ? SoErrorEnum.UNKNOWN.getMessage() : error.getMessage(),
                ex, formatMsg
        );
    }

    /**
     * <h2>通过 {@link SoError} 和 {@link Throwable} 构建一个业务异常类</h2>
     *
     * @param errorCode 异常编码
     * @param errorMsg  异常信息
     * @param ex        原始异常类
     * @param formatMsg errorMsg 数据格式化参数
     */
    public SoBizException(String errorCode, String errorMsg, Throwable ex, Object... formatMsg) {
        super(ex);
        this.errorCode = errorCode;
        if (formatMsg == null || formatMsg.length == 0) {
            this.errorMsg = errorMsg;
        } else {
            try {
                this.errorMsg = String.format(errorMsg, formatMsg);
            } catch (MissingFormatArgumentException exception) {
                this.errorMsg = errorMsg;
            }
        }
    }


    @Override
    public String getCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return errorMsg;
    }
}
