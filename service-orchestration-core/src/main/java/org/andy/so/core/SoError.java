package org.andy.so.core;

/**
 * <h2>错误定义接口，定义错误码和错误信息</h2>
 * 错误枚举定义、自定义异常类等错误流程实现类都需要实现该接口
 *
 * @author: andy
 */
public interface SoError {
    /**
     * <h2>错误编码定义</h2>
     *
     * @return 错误码
     */
    String getCode();

    /**
     * <h2>错误说明信息</h2>
     *
     * @return 错误信息
     */
    String getMessage();
}
