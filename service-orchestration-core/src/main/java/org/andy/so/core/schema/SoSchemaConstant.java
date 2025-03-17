package org.andy.so.core.schema;

/**
 * <h2>schema 常量定义，用来代替 SchemaConstant</h2>
 *
 * @author: andy
 */
public class SoSchemaConstant {
    /**
     * 框架 xml 配置命名空间
     */
    public static final String NAME_SPACE = "https://github.com/andy-wys/service-orchestration";

    /**
     * key value 连接符
     */
    public static final String KEY_VALUE_CONNECTOR = "=";
    /**
     * merchant 节点名称
     */
    public static final String MERCHANT_NODE_NAME = "merchant";
    /**
     * 配置分隔符，用 "," 分开
     */
    public final static String SPLIT_CHAR_CONFIG = ",";
    /**
     * mock 文件数据
     */
    public final static String MOCK_FILE_DATA = "mock-data";
    /**
     * mock 文件 header 数据
     */
    public final static String MOCK_FILE_HEADER = "mock-header";
    /**
     * mock 文件 cookie 数据
     */
    public final static String MOCK_FILE_COOKIE = "mock-cookie";
    /**
     * mock 文件 condition 数据
     */
    public final static String MOCK_FILE_CONDITION = "mock-condition";
}
