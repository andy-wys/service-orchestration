package org.andy.so.core.schema.enums;

import java.util.Arrays;

/**
 * <h2>条件连接类型</h2>
 *
 * @author: andy
 */
public enum SoUnionTypeEnum {
    /**
     * X and Y：且操作，两者都成立则为 {@code true}
     */
    AND,
    /**
     * X or Y：与操作，有一个成立则为 {@code true}
     */
    OR,
    ;

    /**
     * 根据名称匹配枚举类型，忽略大小写
     *
     * @param name 枚举名称
     * @return 未匹配到则返回 {@code null}
     */
    public static SoUnionTypeEnum of(String name) {
        return Arrays.stream(values())
                .filter(e -> e.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
