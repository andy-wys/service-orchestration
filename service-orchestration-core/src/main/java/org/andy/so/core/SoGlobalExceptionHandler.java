package org.andy.so.core;

import org.andy.so.core.entity.SoResp;
import org.andy.so.core.error.SoBizException;
import org.andy.so.core.error.SoErrorEnum;
import org.andy.so.core.error.SoServiceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <h2>全局异常处理类</h2>
 *
 * @author: andy
 */
@RestControllerAdvice
public class SoGlobalExceptionHandler {
    private final Log log = LogFactory.getLog(getClass());

    /**
     * <h2>处理 {@link SoBizException} 异常</h2>
     *
     * @param e 异常
     * @return 标准出参
     */
    @ExceptionHandler(SoBizException.class)
    public SoResp<?> baseException(SoBizException e) {
        log.error("异常：", e);
        return SoResp.buildError(e);
    }

    /**
     * <h2>处理 {@link SoServiceException} 异常</h2>
     *
     * @param e 异常
     * @return 标准出参
     */
    @ExceptionHandler(SoServiceException.class)
    public SoResp<?> baseException(SoServiceException e) {
        log.error("异常：", e);
        return SoResp.buildError(e);
    }

    /**
     * <h2>处理 {@link Exception} 异常，均返回系统未知错误</h2>
     *
     * @param e 异常
     * @return 标准出参
     */
    @ExceptionHandler(Exception.class)
    public SoResp<?> baseException(Exception e) {
        log.error("异常：", e);
        return SoResp.buildError(SoErrorEnum.UNKNOWN);
    }
}
