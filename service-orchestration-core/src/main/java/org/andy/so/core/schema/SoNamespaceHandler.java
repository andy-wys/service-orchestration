package org.andy.so.core.schema;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * <h2>自定义网关 NamespaceHandler</h2>
 *
 * @author: andy
 */
public class SoNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        this.registerBeanDefinitionParser(SoSchemaConstant.MERCHANT_NODE_NAME, new SoMerchantBeanDefinitionParser());
    }
}
