package org.andy.so.core.schema.enums;

import java.util.Arrays;

/**
 * <h2>api 子节点类型</h2>
 *
 * @author: andy
 */
public enum SoServiceNodeChildTypeEnum {
    /**
     * 请求数据配置节点
     */
    REQ,
    /**
     * 返回数据配置节点
     */
    RESP,
    /**
     * 执行条件配置节点
     */
    CONDITION,
    /**
     * mock 数据配置节点
     */
    MOCK;

    /**
     * 根据名称匹配枚举类型，忽略大小写
     *
     * @param name 枚举名称
     * @return 未匹配到则返回 {@code null}
     */
    public static SoServiceNodeChildTypeEnum of(String name) {
        return Arrays.stream(values())
                .filter(e -> e.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

//    /**
//     * 当前枚举定义是否包含该名称，忽略大小写
//     *
//     * @param name 名称
//     * @return {@code true} 包含，否则不包含
//     */
//    @SuppressWarnings("unused")
//    public static Boolean contains(String name) {
//        return Arrays.stream(values()).filter(e -> e.name().equalsIgnoreCase(name))
//                .findFirst().orElse(null) != null;
//    }
}
