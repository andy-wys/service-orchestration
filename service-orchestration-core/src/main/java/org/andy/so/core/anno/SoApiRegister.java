package org.andy.so.core.anno;

import org.andy.so.core.SoApiCommonHandler;
import org.andy.so.core.SoApiService;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * <h2>网关服务接口注册</h2>
 * 继承于 Spring {@link Component}，便于 spring 的管理和兼容<p>
 *
 * <li>该注解定义的服务都必须实现 {@link SoApiService#handle} 方法</li>
 * <li>该注解定义的服务类可以被自动扫描并注册到服务管理</li>
 * <li>该注解定义的服务类默认不使用 XML 配置转换</li>
 * <li>path 为该服务对应的接口地址，可以是多个地址</li>
 * <li>merchantCode 为该服务对应的商户编码</li>
 * <li>该服务定义的服务默认数据格式都是 JSON，在 {@link SoGet} 和 {@link SoPost} 注解可以修改</li>
 *
 * @author: andy
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@SuppressWarnings("unused")
public @interface SoApiRegister {
    /**
     * <h2>访问路径，用于接口自动注册</h2>
     *
     * @return 访问路径
     */
    @AliasFor("requestPath")
    String[] value() default {};

    /**
     * <h2>访问路径，用于接口自动注册</h2>
     *
     * @return 访问路径
     */
    @AliasFor("value")
    String[] requestPath() default {};

    /**
     * <h2>商户编码，用于接口自动注册</h2>
     *
     * @return 商户编码
     */
    String merchantCode() default "";

    /**
     * <h2>该服务类是否注册 request mapping，以供通用 api 方式调用</h2>
     * 默认 {@code true}，即可以通过 {@link SoApiCommonHandler#handleMerchantApiPath} 实现中的{@code {so.merchant.api-root-path:}/{merchantCode}/{apiPath}} 方式调用
     *
     * @return true：自动注册；false 不注册
     */
    boolean isRegRequestMapping() default true;
}
