package org.andy.so.core.schema.enums;

import org.andy.so.core.schema.SoServiceNodeType;

import java.util.Arrays;

/**
 * api 节点的类型
 *
 * @author: andy
 */
public enum SoServiceNodeTypeEnum implements SoServiceNodeType {
    API, REDIRECT, JSF, LOCAL, REF, MOCK;


    public static SoServiceNodeTypeEnum of(String name) {
        return Arrays.stream(values())
                .filter(e -> e.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getServiceNodeType() {
        return name();
    }
}
