package org.andy.so.core.anno;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.*;

/**
 * <h2>GET 请求方法注解，兼容 {@link RequestMapping}</h2>
 *
 * @author: andy
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RequestMapping(method = RequestMethod.GET)
@SuppressWarnings("unused")
public @interface SoGet {

    /**
     * Alias for {@link RequestMapping#path}.
     *
     * @return see RequestMapping
     */
    @AliasFor(annotation = RequestMapping.class)
    String[] path() default {};

    /**
     * Alias for {@link RequestMapping#params}.
     *
     * @return see RequestMapping
     */
    @AliasFor(annotation = RequestMapping.class)
    String[] params() default {};

    /**
     * Alias for {@link RequestMapping#consumes}.
     *
     * @return see RequestMapping
     */
    @AliasFor(annotation = RequestMapping.class)
    String[] consumes() default {};

    /**
     * Alias for {@link RequestMapping#produces}.
     *
     * @return see RequestMapping
     */
    @AliasFor(annotation = RequestMapping.class)
    String[] produces() default {};
}
