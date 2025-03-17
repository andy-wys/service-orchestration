package org.andy.so.core.schema.enums;

import java.util.Arrays;

/**
 * <h2>接口支持的数据传输格式</h2>
 *
 * @author: andy
 */
public enum SoApiContentTypeEnum {
    JSON("application", "json"),
    FORM("application", "x-www-form-urlencoded"),
    TEXT_HTML("text", "html"),
    TEXT_PLAIN("text", "plain");

    private final String type;
    private final String subtype;

    SoApiContentTypeEnum(String type, String subtype) {
        this.type = type;
        this.subtype = subtype;
    }

    public static SoApiContentTypeEnum of(String type) {
        return Arrays.stream(values())
                .filter(e -> e.name().equalsIgnoreCase(type))
                .findFirst()
                .orElse(SoApiContentTypeEnum.JSON);
    }

    public String getType() {
        return type;
    }

    public String getSubtype() {
        return subtype;
    }
}
