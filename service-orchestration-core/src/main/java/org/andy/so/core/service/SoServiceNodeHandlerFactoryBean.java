package org.andy.so.core.service;

import org.andy.so.core.SoMerchantFactory;
import org.andy.so.core.SoMerchantProperty;
import org.andy.so.core.error.SoCheckErrorEnum;
import org.andy.so.core.error.SoCheckException;
import org.andy.so.core.help.SoConditionHandleHelper;
import org.andy.so.core.help.SoDataHandleHelper;
import org.andy.so.core.help.SoMockHelper;
import org.andy.so.core.help.SoParamConvertHelper;
import org.andy.so.core.service.impl.SoNodeMockHandler;
import org.andy.so.core.service.impl.SoNodeServiceDefaultHandler;

/**
 * <h2>SoHandleNodeService 实例的注入</h2>
 *
 * @author: andy
 */
@SuppressWarnings("unused")
public class SoServiceNodeHandlerFactoryBean {
    /**
     * 全局的配置
     */
    private final SoMerchantProperty soMerchantProperty;
    /**
     * 数据处理器工厂帮助类，查找并执行数据处理器
     */
    SoDataHandleHelper soDataHandleHelper;
    /**
     * 条件处理器帮助类，查找并执行条件处理器
     */
    SoConditionHandleHelper soConditionHandleHelper;
    /**
     * 查找并执行参数转换
     */
    SoParamConvertHelper soParamConvertHelper;
    /**
     * mock 数据帮助类
     */
    SoMockHelper soMockHelper;
    /**
     * 配置工厂
     */
    SoMerchantFactory soMerchantFactory;

    public SoServiceNodeHandlerFactoryBean(SoMerchantProperty soMerchantProperty,
                                           SoDataHandleHelper soDataHandleHelper,
                                           SoConditionHandleHelper soConditionHandleHelper,
                                           SoParamConvertHelper soParamConvertHelper,
                                           SoMerchantFactory soMerchantFactory,
                                           SoMockHelper soMockHelper) {
        this.soMerchantProperty = soMerchantProperty;
        this.soDataHandleHelper = soDataHandleHelper;
        this.soConditionHandleHelper = soConditionHandleHelper;
        this.soParamConvertHelper = soParamConvertHelper;
        this.soMerchantFactory = soMerchantFactory;
        this.soMockHelper = soMockHelper;
    }

    /**
     * 根据是否开启 mock 条件实例化默认的  SoHandleNodeService
     *
     * @return SoHandleNodeService 实现
     */
    public SoServiceNodeHandler createHandleNodeService() {
        if (soMerchantProperty == null) {
            throw new SoCheckException(SoCheckErrorEnum.INIT_SERVICE_ERROR, null,
                    SoServiceNodeHandler.class.getSimpleName(), "soMerchantProperty 不能为 null");
        }
        return soMerchantProperty.isMockEnable() ?
                new SoNodeMockHandler(soDataHandleHelper,
                        soConditionHandleHelper,
                        soParamConvertHelper,
                        soMerchantFactory,
                        soMockHelper) :
                new SoNodeServiceDefaultHandler(soDataHandleHelper,
                        soConditionHandleHelper,
                        soParamConvertHelper,
                        soMerchantFactory);
    }
}
