package org.andy.so.core.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.andy.so.core.schema.enums.SoApiCharsetEnum;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * <h2>URI 工具类</h2>
 *
 * @author: andy
 */
public class SoUriUtil {
    private static final String PARAM_SYMBOL = "?";
    private static final String PARAM_JOINT = "&";
    private static final String PARAM_EQUAL = "=";

    /**
     * <h3>拼接 get 请求的连接和参数</h3>
     *
     * @param url       地址
     * @param paramBody 参数
     * @param charset   字符编码
     * @return get 地址
     */
    public static String convertUrlParam(String url, Object paramBody, String charset) {
        String paramStr = convertUrlParam(paramBody, charset);
        if (SoStringUtil.isBlank(paramStr)) {
            return url;
        }
        if (url == null) {
            return paramStr;
        }
        if (url.contains(PARAM_SYMBOL)) {
            return url + PARAM_JOINT + paramStr;
        }
        return url + PARAM_SYMBOL + paramStr;
    }


    /**
     * <h3>拼接 get 请求的连接和参数</h3>
     *
     * @param paramBody 参数
     * @param charset   字符编码
     * @return get 地址
     */
    public static String convertUrlParam(Object paramBody, String charset) {
        if (paramBody == null) {
            return null;
        }
        if (SoStringUtil.isEmpty(charset)) {
            charset = SoApiCharsetEnum.UTF8.getCode();
        }
        if (!JSON.isValidObject(JSON.toJSONString(paramBody))) {
            return String.valueOf(paramBody);
        }

        JSONObject getParam = JSON.parseObject(JSON.toJSONString(paramBody));
        if (getParam == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        try {
            for (Map.Entry<String, Object> entry : getParam.entrySet()) {
                sb.append(URLEncoder.encode(entry.getKey(), charset));
                sb.append(PARAM_EQUAL);
                sb.append(URLEncoder.encode(String.valueOf(entry.getValue()), charset));
                sb.append(PARAM_JOINT);
            }
        } catch (UnsupportedEncodingException ignored) {
        }
        return sb.toString();
    }
}
