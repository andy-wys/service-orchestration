package org.andy.so.core;

import org.andy.so.core.anno.SoApiRegister;
import org.andy.so.core.entity.SoResp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <h2>代码方式定义一个网关服务，会自动注册到 request mapping 中供外部调用</h2>
 * <h3>生效条件：</h3>
 * <pre>1. 实现本接口；</pre>
 * <pre>2. 实现类要用 {@link SoApiRegister} 注解标记；</pre>
 *
 * @author: andy
 */
public interface SoApiService<P, R> {

    /**
     * <h2>业务处理方法</h2>
     *
     * @param request  请求
     * @param response 响应
     * @param param    请求参数
     * @return 标准返回数据对象
     */
    SoResp<R> handle(HttpServletRequest request, HttpServletResponse response, P param);
}
