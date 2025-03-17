package org.andy.so.core.starter;

import org.andy.so.core.*;
import org.andy.so.core.help.SoConditionHandleHelper;
import org.andy.so.core.help.SoDataHandleHelper;
import org.andy.so.core.help.SoMockHelper;
import org.andy.so.core.help.SoParamConvertHelper;
import org.andy.so.core.help.impl.SoConditionHandleHelperImpl;
import org.andy.so.core.help.impl.SoDataHandleHelperImpl;
import org.andy.so.core.help.impl.SoMockHelperImpl;
import org.andy.so.core.help.impl.SoParamConvertHelperImpl;
import org.andy.so.core.service.SoMerchantServiceHandler;
import org.andy.so.core.service.SoServiceNodeHandler;
import org.andy.so.core.service.impl.SoMerchantServiceDefaultHandler;
import org.andy.so.core.service.impl.SoNodeMockHandler;
import org.andy.so.core.service.impl.SoNodeServiceDefaultHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * <h2>自动装配</h2>
 *
 * @author: andy
 */
@Import({SoMerchantProperty.class, SoApplicationContextAware.class, SoEnvironmentAware.class})
@Configuration
public class SoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SoGlobalExceptionHandler.class)
    public SoGlobalExceptionHandler fgGlobalExceptionHandler() {
        return new SoGlobalExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean(SoApiCommonHandler.class)
    public SoApiCommonHandler fgApiCommonHandler() {
        return new SoApiCommonHandler();
    }


    @Bean
    @ConditionalOnMissingBean(SoServiceNodeHandler.class)
    public SoServiceNodeHandler fgHandleNodeService(SoMerchantProperty fgMerchantProperty,
                                                    SoDataHandleHelper fgDataHandleHelper,
                                                    SoConditionHandleHelper fgConditionHandleHelper,
                                                    SoParamConvertHelper fgParamConvertHelper,
                                                    SoMockHelper fgMockHelper,
                                                    SoMerchantFactory fgMerchantFactory) {
        SoServiceNodeHandler handleNodeService;
        if (fgMerchantProperty.isMockEnable()) {
            handleNodeService = new SoNodeMockHandler(
                    fgDataHandleHelper,
                    fgConditionHandleHelper,
                    fgParamConvertHelper,
                    fgMerchantFactory,
                    fgMockHelper);
        } else {
            handleNodeService = new SoNodeServiceDefaultHandler(
                    fgDataHandleHelper,
                    fgConditionHandleHelper,
                    fgParamConvertHelper,
                    fgMerchantFactory);
        }
        return handleNodeService;
    }


    @Bean
    @ConditionalOnMissingBean(SoMerchantFactory.class)
    public SoMerchantFactory fgMerchantFactory(SoMerchantProperty fgMerchantProperty) {
        return new SoMerchantFactory(fgMerchantProperty);
    }

    @Bean
    @ConditionalOnMissingBean(SoMerchantServiceHandler.class)
    public SoMerchantServiceHandler fgHandleMerchantService(SoMerchantFactory fgMerchantFactory, SoServiceNodeHandler fgServiceNodeHandler) {
        return new SoMerchantServiceDefaultHandler(fgMerchantFactory, fgServiceNodeHandler);
    }

    @Bean
    @ConditionalOnMissingBean(SoParamConvertHelper.class)
    public SoParamConvertHelper paramConvertHelper(SoDataHandleHelper fgDataHandleHelper) {
        return new SoParamConvertHelperImpl(fgDataHandleHelper);
    }

    @Bean
    @ConditionalOnMissingBean(SoDataHandleHelper.class)
    public SoDataHandleHelper fgDataHandleFactory() {
        return new SoDataHandleHelperImpl();
    }

    @Bean
    @ConditionalOnMissingBean(SoMockHelper.class)
    public SoMockHelper mockHelper(SoMerchantProperty merchantProperty) {
        return new SoMockHelperImpl(merchantProperty);
    }

    @Bean
    @ConditionalOnMissingBean(SoConditionHandleHelper.class)
    public SoConditionHandleHelper conditionHandleHelper(SoParamConvertHelper paramConvertHelper, SoDataHandleHelper dataConvertHandleHelper) {
        return new SoConditionHandleHelperImpl(paramConvertHelper, dataConvertHandleHelper);
    }


}
