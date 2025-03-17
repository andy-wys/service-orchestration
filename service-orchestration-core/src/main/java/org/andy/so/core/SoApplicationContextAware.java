package org.andy.so.core;

import org.andy.so.core.trace.SoTraceConstant;
import org.andy.so.core.util.SoObjectUtil;
import org.andy.so.core.util.SoStringUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

/**
 * <h2>应用上下文包装类</h2>
 *
 * @author: andy
 */
public class SoApplicationContextAware implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    /**
     * <h2>初始化 spring application context</h2>
     *
     * @param applicationContext spring application context
     * @throws BeansException 异常
     */
    @Override
    @SuppressWarnings("all")
    public void setApplicationContext(ApplicationContext applicationContext) {
        SoApplicationContextAware.applicationContext = applicationContext;
        printSoBanner();
    }

    /**
     * <h2>通过 bean 名称查询 bean 实例</h2>
     *
     * @param beanName 名称
     * @return 实例
     */
    public static Object getBean(String beanName) {
        if (SoStringUtil.isBlank(beanName)) {
            return null;
        }
        if (applicationContext.containsBean(beanName)) {
            return applicationContext.getBean(beanName);
        } else {
            return null;
        }
    }

    /**
     * <h2>根据类型获取 spring bean</h2>
     *
     * @param requiredType class type
     * @param <T>          泛型
     * @return spring bean
     */
    public static <T> T getBean(Class<T> requiredType) {
        if (requiredType == null) {
            return null;
        }
        try {
            return applicationContext.getBean(requiredType);
        } catch (BeansException ignored) {
        }
        return null;
    }

    /**
     * <h2>通过 bean name 和 bean 类型获取 spring bean</h2>
     *
     * @param beanName bean name
     * @param baseType bean type
     * @param <T>      bean type
     * @return spring bean instance
     */
    public static <T> T getBean(String beanName, Class<T> baseType) {
        if (SoObjectUtil.anyNull(beanName, baseType)) {
            return null;
        }
        return applicationContext.getBean(beanName, baseType);
    }

    /**
     * <h2>根据 bean type 获取 spring bean</h2>
     *
     * @param baseType bean type
     * @param <T>      bean type
     * @return spring bean instance
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> baseType) {
        if (baseType == null) {
            return null;
        }
        return applicationContext.getBeansOfType(baseType);
    }

    /**
     * <h2>通过注解获取 bean 集合</h2>
     *
     * @param annoType 注解类型
     * @return map of spring bean instance
     */
    public static Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annoType) {
        if (annoType == null) {
            return null;
        }
        return applicationContext.getBeansWithAnnotation(annoType);
    }

    /**
     * 框架启动 banner 打印
     */
    private static void printSoBanner() {
        String pomPropFile = "/META-INF/maven/org.andy.so/finance-gateway-core/pom.properties";
        try (InputStream pomPropStream = SoApplicationContextAware.class.getResourceAsStream(pomPropFile)) {
            SoMerchantProperty property = getBean(SoMerchantProperty.class);
            if (property == null || !property.isBannerLog()) {
                return;
            }
            Properties properties = new Properties();
            properties.load(pomPropStream);
            String version = properties.getProperty("version");

            String lineBr = "\n";
            String lineStr = "|-------------------------------------------------------------------------------------\n";
            String lineSlogan = "|------------ 非常感谢您使用网关框架: 前行的路上，没有终点，只有不断追求的过程! ------------------\n";
            StringBuilder sb = new StringBuilder()
                    .append(lineStr)
                    .append(lineSlogan)
                    .append(lineStr)
                    .append("| 当前框架版本   | ")
                    .append(version.endsWith("-SNAPSHOT") ? version + " (建议使用 RELEASE 版本)" : version)
                    .append(lineBr)
                    .append("| 默认服务编码   | ")
                    .append(SoStringUtil.isBlank(property.getCode()) ? "未配置" : property.getCode())
                    .append(lineBr)
                    .append("| 接口访问路径   | ")
                    .append("{host}/{server.servlet.context-path}")
                    .append(SoStringUtil.isNotBlank(property.getApiRootPath()) ? "/" + property.getApiRootPath() : "")
                    .append("/{merchantCode}/{apiPath}")
                    .append(lineBr)
                    .append("| 接口配置文件   | ")
                    .append(Arrays.toString(property.getApiFile()))
                    .append(lineBr)
                    .append("| 是否开启 MOCK | ")
                    .append(property.isMockEnable() ? "false: 未开启" : "true: 已开启")
                    .append(lineBr)
                    .append("| MOCK 文件目录 | ")
                    .append(property.getMockDataDir())
                    .append(lineBr)
                    .append("| 链路追踪 KEY  | ")
                    .append(SoStringUtil.isBlank(property.getTraceKey()) ? SoTraceConstant.KEY_TRACE_ID : property.getTraceKey())
                    .append(lineBr)
                    .append(lineStr);
            System.out.println(sb);
        } catch (Exception ignored) {
        }
    }
}
