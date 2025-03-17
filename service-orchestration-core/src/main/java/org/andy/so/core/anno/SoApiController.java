package org.andy.so.core.anno;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.*;

/**
 * <h2>网关服务 controller 注解</h2>
 * 继承于 Spring RestController，便于 spring 的管理和兼容
 *
 * @author: andy
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RestController
@SuppressWarnings("unused")
public @interface SoApiController {
    @AliasFor(
            annotation = RestController.class
    )
    String value() default "";
}
