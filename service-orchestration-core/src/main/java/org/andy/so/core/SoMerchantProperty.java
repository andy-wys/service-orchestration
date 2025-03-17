package org.andy.so.core;

import org.andy.so.core.trace.SoTraceConstant;
import org.andy.so.core.util.SoStringUtil;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;

/**
 * <h2>商户配置信息</h2>
 *
 * @author: andy
 */
@SuppressWarnings("unused")
public class SoMerchantProperty {
    /**
     * <h2>全局默认商户服务编码</h2>
     * 此处配置则在对应的 XML 中则可省略 code 配置
     */
    @Value("${so.merchant.code: }")
    private String code;
    /**
     * <h2>全局默认商户服务名称，可选</h2>
     */
    @Value("${so.merchant.name: }")
    private String name;
    /**
     * <h2>商户接口访问根路径</h2>
     */
    @Value("${so.merchant.api-root-path: }")
    private String apiRootPath;
    /**
     * <h2>商户接口配置文件，默认 classpath:api/*.xml</h2>
     */
    @Value("${so.merchant.api-file: classpath:api/*.xml}")
    private String[] apiFile = new String[]{"classpath:api/*.xml"};
    /**
     * <h2>是否开启 mock 数据</h2>
     */
    @Value("${so.merchant.mock-enable: false}")
    private boolean mockEnable = false;
    /**
     * <h2>mock 数据文件存放目录，默认 classpath:api-mock/</h2>
     */
    @Value("${so.merchant.mock-data-dir: classpath:api-mock/}")
    private String mockDataDir = "classpath:api-mock/";
    /**
     * <h2>链路追踪的 traceId</h2>
     */
    @Value("${so.merchant.trace-key: }")
    private String traceKey;
    /**
     * <h2>是否打印框架 banner</h2>
     */
    @Value("${so.merchant.log.banner: true}")
    private boolean bannerLog = true;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApiRootPath() {
        return apiRootPath;
    }

    public void setApiRootPath(String apiRootPath) {
        this.apiRootPath = apiRootPath;
    }

    public String[] getApiFile() {
        return apiFile;
    }

    public void setApiFile(String[] apiFile) {
        this.apiFile = apiFile;
    }

    public boolean isMockEnable() {
        return mockEnable;
    }

    public void setMockEnable(boolean mockEnable) {
        this.mockEnable = mockEnable;
    }

    public String getMockDataDir() {
        return mockDataDir;
    }

    public void setMockDataDir(String mockDataDir) {
        this.mockDataDir = mockDataDir;
    }

    public String getTraceKey() {
        return traceKey;
    }

    public void setTraceKey(String traceKey) {
        this.traceKey = traceKey;
        if (SoStringUtil.isNotBlank(traceKey)) {
            SoTraceConstant.KEY_TRACE_ID = traceKey;
        }
    }

    public boolean isBannerLog() {
        return bannerLog;
    }

    public void setBannerLog(boolean bannerLog) {
        this.bannerLog = bannerLog;
    }

    @Override
    public String toString() {
        return "SoMerchantProperty{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", apiRootPath='" + apiRootPath + '\'' +
                ", apiFile=" + Arrays.toString(apiFile) +
                ", mockEnable=" + mockEnable +
                ", mockDataDir='" + mockDataDir + '\'' +
                ", traceKey='" + traceKey + '\'' +
                ", bannerLog=" + bannerLog +
                '}';
    }
}
