package org.andy.so.core.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

/**
 * json 工具类，避免异常
 *
 * @author: andy
 */
@SuppressWarnings("unused")
public final class SoJsonUtil {
    /**
     * 从 JSON 对象中取值
     *
     * @param jsonObject JSON obj
     * @param key        key
     * @return str
     */
    public static String getString(JSONObject jsonObject, String key) {
        if (jsonObject == null || SoStringUtil.isBlank(key) || !jsonObject.containsKey(key)) {
            return null;
        }
        return jsonObject.getString(key);
    }

    /**
     * 从 JSON 对象中取值
     *
     * @param jsonObject json obj
     * @param key        key
     * @return obj
     */
    public static Object getObject(JSONObject jsonObject, String key) {
        if (jsonObject == null || SoStringUtil.isBlank(key) || !jsonObject.containsKey(key)) {
            return null;
        }
        return jsonObject.get(key);
    }

    /**
     * 获取 JSON 对象
     *
     * @param jsonObject source json
     * @param key        json key
     * @return json object value
     */
    public static JSONObject getJsonObject(JSONObject jsonObject, String key) {
        if (jsonObject == null || SoStringUtil.isBlank(key) || !jsonObject.containsKey(key)) {
            return null;
        }
        return jsonObject.getJSONObject(key);
    }

    /**
     * 获取 JSON array
     *
     * @param jsonObject source json
     * @param key        json key
     * @return json array value
     */
    public static JSONArray getJsonArray(JSONObject jsonObject, String key) {
        if (jsonObject == null || SoStringUtil.isBlank(key) || !jsonObject.containsKey(key)) {
            return null;
        }
        return jsonObject.getJSONArray(key);
    }

    /**
     * 合并 source 和 target 数据，优先使用 source
     *
     * @param source 源数据
     * @param target 目标数据
     * @return 合并后的结果
     */
    public static Object merge(Object source, Object target) {
        if (source == null) {
            return target;
        }
        if (target == null) {
            return source;
        }
        Object sourceJson = JSON.toJSON(source);
        Object targetJson = JSON.toJSON(target);

        if (sourceJson instanceof JSONObject) {
            if (targetJson instanceof JSONObject) {
                return mergeJson((JSONObject) sourceJson, (JSONObject) targetJson);
            }
        } else if (sourceJson instanceof JSONArray) {
            ((JSONArray) sourceJson).add(targetJson);
            return sourceJson;
        }
        return source;
    }

    /**
     * 合并 JSON 对象，用 source 覆盖 target，返回覆盖后的 JSON 对象
     *
     * @param source JSONObject
     * @param target JSONObject
     * @return JSONObject
     */
    public static JSONObject mergeJson(JSONObject source, JSONObject target) {
        // 覆盖目标JSON为空，直接返回覆盖源
        if (target == null) {
            return source;
        }
        if (source == null) {
            return target;
        }

        for (String key : source.keySet()) {
            Object value = source.get(key);
            if (!target.containsKey(key)) {
                target.put(key, value);
            } else {
                if (value instanceof JSONObject) {
                    Object targetJsonObject = target.get(key);
                    if (targetJsonObject instanceof JSONObject) {
                        JSONObject targetValue = mergeJson((JSONObject) value, (JSONObject) targetJsonObject);
                        target.put(key, targetValue);
                    } else {
                        target.put(key, value);
                    }
                } else if (value instanceof JSONArray) {
                    Object targetJsonObject = target.get(key);
                    if (targetJsonObject instanceof JSONArray) {
                        JSONArray targetArray = (JSONArray) targetJsonObject;
                        targetArray.addAll((JSONArray) value);
                        target.put(key, targetArray);
                    } else {
                        target.put(key, value);
                    }
                } else {
                    target.put(key, value);
                }
            }
        }
        return target;
    }
}
