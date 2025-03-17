package org.andy.so.core.error;

import org.andy.so.core.SoError;

/**
 * <h2>服务配置检查错误定义</h2>
 *
 * @author: andy
 */
public enum SoCheckErrorEnum implements SoError {
    XML_CONFIG_ERROR("0010", "XML文件配置错误"),
    API_CONFIG_ERROR("0011", "api服务配置错误"),
    API_PATH_HAS_EXIST_FORMAT("0012", "商户[%1$s]的接口[%2$s]已经在[%3$s]中配置，请勿重复配置"),
    XML_REQ_PATH_HAS_EXIST_FORMAT("0013", "商户[%1$s]的接口[%2$s]已经在XML中配置，请勿重复配置"),
    XML_NODE_ID_HAS_EXIST_FORMAT("0014", "商户[%1$s]的接口[%2$s]配置错误，节点[id=%3$s]重复，会导致数据被覆盖，请修改"),
    DATA_HANDLE_HAS_EXIST("0015", "数据转换器 [convertType = %1$s] 已经存在，请重新命名"),
    UNDEFINED_DO_CONVERT_METHOD("0016", "[%1$s]未正确定义 doConvert 方法，请检查"),
    INIT_SERVICE_ERROR("0017", "初始化 [%1$s] 失败，[%2$s]"),
    UNDEFINED_SERVICE_NODE_PARSER("0018", "未找到节点 [%1$s] 对应的解析器，请检查 XML 配置，或者调用 MerchantFactory#registerServiceNodeParser 方法注册节点解析器。"),

    UNDEFINED_SERVICE_ERROR("0020", "当前服务未正确定义，请检查请求地址或接口配置"),
    UNDEFINED_HANDLE_METHOD("0021", "当前服务实现类未正确定义handle方法"),
    UNREGISTER_SERVICE_ERROR("0022", "当前服务未定义或未注册到 request mapping 中"),

    INIT_DATA_HANDLE_ERROR("0030", "初始化 data handle 失败：%1$s"),
    ;

    private final String code;
    private final String message;

    SoCheckErrorEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
