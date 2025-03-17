package org.andy.so.core.starter;

import org.andy.so.core.error.SoBizException;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * <h2>框架故障分析报告</h2>
 * author: andy
 **/
public class SoFailureAnalyzer extends AbstractFailureAnalyzer<SoBizException> {
    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, SoBizException cause) {
        return new FailureAnalysis(
                cause.getCode() + " - " + cause.getMessage(),
                "服务编排框架配置初始化失败，请检查框架配置后重新启动",
                cause);
    }
}