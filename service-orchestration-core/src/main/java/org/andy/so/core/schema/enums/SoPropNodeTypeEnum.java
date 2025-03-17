package org.andy.so.core.schema.enums;

import java.util.Arrays;

/**
 * <h2>属性节点类型</h2>
 *
 * @author: andy
 */
public enum SoPropNodeTypeEnum {
    /**
     * property 节点类型
     */
    PROPERTY,
    /**
     * header 节点类型
     */
    HEADER,
    /**
     * cookie 节点类型
     */
    COOKIE,
    /**
     * param 节点类型
     */
    PARAM,
    /**
     * compare 节点类型
     */
    COMPARE;

    /**
     * 根据名称匹配枚举类型
     *
     * @param name 名称，忽略大小写
     * @return 未匹配到则返回 null
     */
    public static SoPropNodeTypeEnum of(String name) {
        return Arrays.stream(values())
                .filter(e -> e.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
