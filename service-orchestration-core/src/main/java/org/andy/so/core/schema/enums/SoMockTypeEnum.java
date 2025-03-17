package org.andy.so.core.schema.enums;

import java.util.Arrays;

/**
 * <h2>mock 数据类型</h2>
 *
 * @author: andy
 */
public enum SoMockTypeEnum {
    /**
     * 请求数据
     */
    REQ,
    /**
     * 响应数据
     */
    RESP;

    /**
     * 将 name 转换成功枚举类型(忽略大小写)，未匹配到则返回 null
     *
     * @param name 枚举名称
     * @return 未匹配到则返回 {@code null}
     */
    public static SoMockTypeEnum of(String name) {
        return Arrays.stream(values())
                .filter(e -> e.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
