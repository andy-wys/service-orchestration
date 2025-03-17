package org.andy.so.core.schema.enums;

import java.util.Arrays;

/**
 * <h2>条件操作类型</h2>
 *
 * @author: andy
 */
public enum SoConditionTypeEnum {
    /**
     * equals 操作，大小写敏感
     */
    EQUAL,
    /**
     * equals 操作，忽略大小写
     */
    EQUAL_IGNORE_CASE,
    /**
     * 大于
     */
    GREATER_THAN,
    /**
     * 大于或等于
     */
    GREATER_EQUAL,
    /**
     * 小于
     */
    LESS_THAN,
    /**
     * 小于或等于
     */
    LESS_EQUAL,
    /**
     * 包含
     */
    IN,
    /**
     * 不包含
     */
    NOT_IN,
    /**
     * 区间范围，between ... and ...
     */
    BETWEEN,
    ;

    /**
     * 根据名称匹配对应的枚举类型，忽略大小写
     *
     * @param name 枚举名称
     * @return 未匹配到则返回 {@code null}
     */
    public static SoConditionTypeEnum of(String name) {
        return Arrays.stream(values())
                .filter(e -> e.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
