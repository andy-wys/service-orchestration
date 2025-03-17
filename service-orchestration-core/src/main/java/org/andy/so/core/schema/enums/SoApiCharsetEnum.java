package org.andy.so.core.schema.enums;

import java.util.Arrays;

/**
 * <h2>支持的字符编码定义</h2>
 * 字符编码，目前只支持 UTF8，后续有要求再添加
 *
 * @author: andy
 */
public enum SoApiCharsetEnum {
    UTF8("UTF8"),
    GBK("GBK"),
    ;

    private final String code;

    SoApiCharsetEnum(String code) {
        this.code = code;
    }

    public static SoApiCharsetEnum of(String code) {
        return Arrays.stream(values())
                .filter(e -> e.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    public String getCode() {
        return code;
    }
}
