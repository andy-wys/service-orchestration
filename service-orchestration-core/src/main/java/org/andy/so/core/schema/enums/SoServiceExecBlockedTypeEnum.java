package org.andy.so.core.schema.enums;

import java.util.Arrays;

/**
 * <h2>接口阻断类型</h2>
 * 定义当执行节点出现异常时接口的下一步操作行为
 *
 * @author: andy
 */
public enum SoServiceExecBlockedTypeEnum {
    /**
     * 当执行节点出现异常时返回已处理过的数据
     */
    RETURN,
    /**
     * 当执行节点出现异常时将异常抛出
     */
    EXCEPTION,
    /**
     * 当执行节点出现异常时忽略异常，继续执行下一个执行节点
     */
    CONTINUE;

    /**
     * 根据名称匹配枚举类型，忽略大小写
     *
     * @param name 枚举名称
     * @return 未匹配到则返回 {@code null}
     */
    public static SoServiceExecBlockedTypeEnum of(String name) {
        return Arrays.stream(values())
                .filter(e -> e.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(EXCEPTION);
    }
}
