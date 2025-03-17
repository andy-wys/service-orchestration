package org.andy.so.core.help.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.andy.so.core.SoMerchantProperty;
import org.andy.so.core.help.SoMockHelper;
import org.andy.so.core.schema.SoSchemaConstant;
import org.andy.so.core.schema.enums.SoMockTypeEnum;
import org.andy.so.core.schema.node.SoMockNode;
import org.andy.so.core.service.SoExecNodeServiceData;
import org.andy.so.core.util.SoJsonUtil;
import org.andy.so.core.util.SoStringUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StreamUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h2>mock 数据帮助类默认实现</h2>
 *
 * @author: andy
 */
public class SoMockHelperImpl implements SoMockHelper {
    private final Log log = LogFactory.getLog(getClass());
    /**
     * 标准接口配置信息
     */
    SoMerchantProperty merchantProperty;

    /**
     * 缓存 mock 数据
     */
    private final Map<String, JSONObject> mockDataCache = new HashMap<>();

    /**
     * 根据标准接口配置构造对象
     *
     * @param merchantProperty 标准接口配置
     */
    public SoMockHelperImpl(SoMerchantProperty merchantProperty) {
        this.merchantProperty = merchantProperty;
    }

    /**
     * 是否 mock 响应数据
     *
     * @param mockNode mock 节点配置
     * @return true / false
     */
    @Override
    public boolean isMockRespEnable(SoMockNode mockNode) {
        return isMockEnable(mockNode, SoMockTypeEnum.RESP);
    }

    /**
     * 是否 mock 请求数据
     *
     * @param mockNode mock 节点配置
     * @return true / false
     */
    @Override
    public boolean isMockReqEnable(SoMockNode mockNode) {
        return isMockEnable(mockNode, SoMockTypeEnum.REQ);
    }

    /**
     * mock 开关
     *
     * @param mockNode mock 节点配置
     * @param typeEnum mock 类型
     * @return true 则 mock 生效
     */
    @Override
    public boolean isMockEnable(SoMockNode mockNode, SoMockTypeEnum typeEnum) {
        boolean result = merchantProperty.isMockEnable();
        if (!result || mockNode == null) {
            return false;
        }
        if (typeEnum == null) {
            return mockNode.isEnable();
        } else {
            return mockNode.isEnable() && typeEnum == mockNode.getType();
        }
    }

    /**
     * 转换 mock 的响应数据
     *
     * @param mockNode mock 配置节点
     * @param response HttpServletResponse
     * @return response data
     */
    @Override
    public Object parseMockResp(SoMockNode mockNode, HttpServletResponse response) {
        JSONObject jsonObject = getMockFile(mockNode);
        Object data = getMockData(jsonObject);

        if (response == null) {
            return data;
        }
        JSONObject header = SoJsonUtil.getJsonObject(jsonObject, SoSchemaConstant.MOCK_FILE_HEADER);
        if (header != null) {
            for (String key : header.keySet()) {
                response.addHeader(key, SoJsonUtil.getString(header, key));
            }
        }
        JSONObject cookie = SoJsonUtil.getJsonObject(jsonObject, SoSchemaConstant.MOCK_FILE_COOKIE);
        if (cookie != null) {
            for (String key : cookie.keySet()) {
                response.addCookie(new Cookie(key, SoJsonUtil.getString(cookie, key)));
            }
        }
        return data;
    }

    /**
     * 转换 mock 的执行条件
     *
     * @param mockNode mock 配置节点
     * @return true 允许执行，false 不执行
     */
    @Override
    public boolean parseMockCondition(SoMockNode mockNode) {
        JSONObject jsonObject = getMockFile(mockNode);
        String condition = SoJsonUtil.getString(jsonObject, SoSchemaConstant.MOCK_FILE_CONDITION);
        if (SoStringUtil.isBlank(condition)) {
            return true;
        }
        return Boolean.parseBoolean(condition);
    }

    /**
     * 解析 mock 的请求数据
     *
     * @param mockNode mock 配置
     * @return mock 的请求数据
     */
    @Override
    public Object parseMockReq(SoMockNode mockNode) {
        return getMockData(getMockFile(mockNode));
    }

    /**
     * 解析 mock 的 http 头并设置到 ApiServiceData 中
     *
     * @param mockNode       mock 配置
     * @param apiServiceData 目标值
     */
    @Override
    public void parseMockHeader(SoMockNode mockNode, SoExecNodeServiceData apiServiceData) {
        if (apiServiceData == null) {
            return;
        }
        JSONObject jsonObject = getMockFile(mockNode);

        JSONObject header = SoJsonUtil.getJsonObject(jsonObject, SoSchemaConstant.MOCK_FILE_HEADER);
        if (header != null) {
            Map<String, String> headerMap = apiServiceData.getHttpHeaders();
            if (headerMap == null) {
                headerMap = new HashMap<>(4);
                apiServiceData.setHttpHeaders(headerMap);
            }
            for (String key : header.keySet()) {
                headerMap.put(key, SoJsonUtil.getString(header, key));
            }
        }
        JSONObject cookie = SoJsonUtil.getJsonObject(jsonObject, SoSchemaConstant.MOCK_FILE_COOKIE);
        if (cookie != null) {
            List<String> cookies = apiServiceData.getHttpCookies();
            if (cookies == null) {
                cookies = new ArrayList<>();
                apiServiceData.setHttpCookies(cookies);
            }
            for (String key : cookie.keySet()) {
                cookies.add(key + SoSchemaConstant.KEY_VALUE_CONNECTOR + SoJsonUtil.getString(cookie, key));
            }
        }
    }

    /**
     * 读取 mock 数据
     *
     * @param mockNode mock 配置
     * @return JSON object
     */
    @Override
    public JSONObject getMockFile(SoMockNode mockNode) {
        if (mockNode == null || SoStringUtil.isBlank(mockNode.getFileName())) {
            return null;
        }
        try {
            String fileName = merchantProperty.getMockDataDir() + mockNode.getFileName();
            JSONObject data = mockDataCache.get(fileName);
            if (data != null) {
                return data;
            }

            String fileText = getFileContext(fileName);
            data = JSON.parseObject(fileText);
            mockDataCache.put(fileName, data);
            return data;
        } catch (Exception e) {
            log.error("读取 mock 数据失败，文件名：" + mockNode.getFileName(), e);
        }
        return null;
    }

    /**
     * mock-data 数据，兼容非标准格式
     *
     * @param jsonObject 配置整体
     * @return mock-data
     */
    private Object getMockData(JSONObject jsonObject) {
        Object data = SoJsonUtil.getObject(jsonObject, SoSchemaConstant.MOCK_FILE_DATA);
        if (data == null) {
            return jsonObject;
        }
        return data;
    }

    /**
     * 读取文件内容
     *
     * @param resourceLocation 文件位置
     * @return 文件内容
     * @throws IOException ex
     */
    private String getFileContext(String resourceLocation) throws IOException {
        if (SoStringUtil.isBlank(resourceLocation)) {
            return null;
        }
        if (resourceLocation.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
            String path = resourceLocation.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length());
            String description = "class path resource [" + path + "]";
            ClassLoader cl = ClassUtils.getDefaultClassLoader();
            URL url = (cl != null ? cl.getResource(path) : ClassLoader.getSystemResource(path));
            if (url == null) {
                throw new FileNotFoundException(description + " cannot be resolved to absolute file path because it does not exist");
            }
            // jar:file:/xxx
            if (ResourceUtils.isJarURL(url)) {
                return getJarUrlFileContext(path);
            }
        }

        File file = ResourceUtils.getFile(resourceLocation);
        return readAsString(file);
    }

    /**
     * 从 file 中读取内容
     *
     * @param file 文件对象
     * @return 文件内容
     * @throws IOException IO 异常
     */
    @SuppressWarnings("all")
    private String readAsString(File file) throws IOException {
        BufferedReader r = new BufferedReader(new FileReader(file));
        StringBuffer b = new StringBuffer();
        while (true) {
            int ch = r.read();
            if (ch == -1) {
                r.close();
                return b.toString();
            }
            b.append((char) ch);
        }
    }

    /**
     * 通过流的方式读取文件内容
     *
     * @param fileName 文件名称
     * @return 文件内容
     */
    private String getJarUrlFileContext(String fileName) {
        String pathPre = "/";
        if (!fileName.startsWith(pathPre)) {
            fileName = pathPre + fileName;
        }
        //返回读取指定资源的输入流
        InputStream is = this.getClass().getResourceAsStream(fileName);
        if (is == null) {
            return null;
        }

        try {
            return StreamUtils.copyToString(is, Charset.defaultCharset());
        } catch (IOException e) {
            log.error("读取文件内容失败，文件名：" + fileName, e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                log.error("关闭文件流失败，文件名：" + fileName, e);
            }
        }
        return null;
    }
}
