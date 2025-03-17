package org.andy.so.demo.springboot.handle;

import org.andy.so.core.SoDataHandle;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import java.nio.charset.StandardCharsets;

/**
 * <h2>Base64解码处理器测试代码</h2>
 **/
@Component
public class Base64DecodeHandle implements SoDataHandle<String, String> {
    @Override
    public String getHandleName() {
        return "BASE64_DE";
    }

    @Override
    public String doConvert(String source, Object... objects) {
        if (source == null) {
            return null;
        }
        return new String(Base64Utils.decode(source.getBytes(StandardCharsets.UTF_8)));
    }
}
