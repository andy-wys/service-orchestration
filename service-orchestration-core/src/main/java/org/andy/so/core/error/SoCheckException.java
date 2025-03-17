package org.andy.so.core.error;

import org.andy.so.core.SoError;

/**
 * <h2>服务配置检查异常</h2>
 *
 * @author: andy
 */
public class SoCheckException extends SoBizException {
    private static final long serialVersionUID = -4375507506638008528L;

    public SoCheckException(SoError error) {
        super(error);
    }

    public SoCheckException(SoError error, Throwable ex, Object... msgFormatParam) {
        super(error, ex, msgFormatParam);
    }

    public SoCheckException(String errorMsg, Throwable ex) {
        super(SoCheckErrorEnum.XML_CONFIG_ERROR.getCode(), errorMsg, ex);
    }
}
