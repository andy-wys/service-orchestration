package org.andy.so.core.error;

import org.andy.so.core.SoError;

/**
 * <h2>服务执行异常</h2>
 *
 * @author: andy
 */
public class SoServiceException extends SoBizException {
    private static final long serialVersionUID = 5567738089853685217L;

    public SoServiceException(SoError error) {
        super(error);
    }

    @SuppressWarnings("unused")
    public SoServiceException(SoError error, Throwable ex) {
        super(error, ex);
    }

    public SoServiceException(SoError error, Throwable ex, Object... formatParam) {
        super(error.getCode(), String.format(error.getMessage(), formatParam), ex);
    }
}
