package org.andy.so.core;

import com.alibaba.fastjson2.JSON;
import org.andy.so.core.anno.SoApiRegister;
import org.andy.so.core.error.SoCheckErrorEnum;
import org.andy.so.core.error.SoCheckException;
import org.andy.so.core.schema.SoServiceNodeParser;
import org.andy.so.core.schema.SoServiceNodeType;
import org.andy.so.core.schema.enums.SoServiceNodeTypeEnum;
import org.andy.so.core.schema.node.SoMerchantNode;
import org.andy.so.core.schema.node.SoServiceNode;
import org.andy.so.core.service.SoServiceNodeExecutor;
import org.andy.so.core.service.impl.SoHttpNodeExecutor;
import org.andy.so.core.service.impl.SoRedirectNodeExecutor;
import org.andy.so.core.service.impl.SoRefNodeExecutor;
import org.andy.so.core.service.impl.SoSpringBeanNodeExecutor;
import org.andy.so.core.util.SoStringUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Node;

import java.util.*;

/**
 * <h2>服务配置工厂类，根据商户保存接口配置，便于快速检索服务配置</h2>
 * <i>这里是在项目启动时初始化的，所以不需要考虑线程安全问题</i>
 *
 * @author: andy
 */
@SuppressWarnings("unused")
public class SoMerchantFactory {
    private static final Log log = LogFactory.getLog(SoMerchantFactory.class);
    private static final String PATH_SPLIT_CHAR = ",";
    private final String KEY_XML = "_xml";
    private final String KEY_CODDING = "_codding";
    private final String MERCHANT_ALL = "*";
    /**
     * <h3>XML 方式定义的接口配置，存储到 map 以便快速检索</h3>
     */
    private final HashMap<String, SoMerchantNode> merchantConfigMap = new HashMap<>();
    /**
     * <h3>代码方式定义的接口</h3>
     */
    private final HashMap<String, SoApiService<?, ?>> apiServiceMap = new HashMap<>();
    /**
     * <h3>服务节点解析器</h3>
     */
    private static final Set<SoServiceNodeParser> SERVICE_NODE_PARSER_MAP = new HashSet<>(2);

    /**
     * <h3>服务节点执行器</h3>
     */
    private final Map<SoServiceNodeType, SoServiceNodeExecutor<?>> serviceNodeExecutorMap = new HashMap<>(8);

    public SoMerchantFactory() {
    }

    /**
     * <h3>通过 SoMerchantProperty 来构造配置信息</h3>
     *
     * @param soMerchantProperty 自定义配置
     */
    public SoMerchantFactory(SoMerchantProperty soMerchantProperty) {
        initMerchantConfig(soMerchantProperty);
    }

    /**
     * <h2>通过自定义属性初始化接口配置</h2>
     * <ul>
     *     <li>加载接口配置文件</li>
     *     <li>加载 SoApiService 接口且使用了 SoApiRegister 注解的实现类，加到 apiServiceMap 中</li>
     *     <li>配置按执行顺序排序</li>
     * </ul>
     *
     * @param soMerchantProperty 自定义属性配置
     */
    public void initMerchantConfig(SoMerchantProperty soMerchantProperty) {
        if (soMerchantProperty == null) {
            throw new SoCheckException("不能通过 SoMerchantProperty = [null] 来初始化 SoMerchantFactory 实例", null);
        }
        // XML 配置文件加载接口配置
        String propFile = null;
        try {
            log.info("准备加载接口配置文件：" + JSON.toJSONString(soMerchantProperty.getApiFile()));
            for (String s : soMerchantProperty.getApiFile()) {
                propFile = s;
                log.info("准备在 " + getClass().getSimpleName() + " 类中加载配置文件 [" + s + "] 中的接口配置");
                ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(s);
                Map<String, SoMerchantNode> map = applicationContext.getBeansOfType(SoMerchantNode.class);
                log.debug("SoApiMapConfig = " + JSON.toJSONString(map));
                map.forEach((s1, merchantNode) ->
                        registerMerchantNode(merchantNode)
                );
            }
            log.info("接口配置文件加载完毕...");
        } catch (BeansException e) {
            log.error("解析接口配置文件 " + propFile + " 错误，请检查: " + e.getMessage());
        }
        // 扫描用注解的 SoApiService
        Map<String, Object> apiServiceMap = SoApplicationContextAware.getBeansWithAnnotation(SoApiRegister.class);

        List<Object> apiService = new LinkedList<>(apiServiceMap.values());

        apiService.forEach(v -> {
            if (v == null) {
                return;
            }
            if (!(v instanceof SoApiService)) {
                log.error("服务 [" + v.getClass() + "] 注册必须实现 SoApiService 接口，否则不生效");
                return;
            }
            SoApiService<?, ?> soApiService = (SoApiService<?, ?>) v;

            SoApiRegister serviceRegAnno = soApiService.getClass().getAnnotation(SoApiRegister.class);
            if (serviceRegAnno == null) {
                return;
            }

            String merchantCode = serviceRegAnno.merchantCode();
            String[] pathArr = serviceRegAnno.requestPath();
            if (pathArr == null || pathArr.length == 0) {
                pathArr = serviceRegAnno.value();
            }
            if (pathArr == null) {
                return;
            }
            for (String path : pathArr) {
                registerApiService(merchantCode, path, soApiService);
            }
        });

        // 对接口配置排序
        sortApiConfigOrderAsc();

        // 注册默认的执行器
        registerServiceNodeExecutor(SoServiceNodeTypeEnum.REF, new SoRefNodeExecutor());
        registerServiceNodeExecutor(SoServiceNodeTypeEnum.JSF, new SoSpringBeanNodeExecutor());
        registerServiceNodeExecutor(SoServiceNodeTypeEnum.LOCAL, new SoSpringBeanNodeExecutor());
        registerServiceNodeExecutor(SoServiceNodeTypeEnum.API, new SoHttpNodeExecutor());
        registerServiceNodeExecutor(SoServiceNodeTypeEnum.REDIRECT, new SoRedirectNodeExecutor());
    }

    /**
     * <h2>根据商户编码和请求路径查询接口实现类</h2>
     *
     * @param merchantCode 商户编码
     * @param apiPath      接口地址
     * @return 接口实现类
     */
    public SoApiService<?, ?> getApiService(String merchantCode, String apiPath) {
        if (SoStringUtil.isBlank(apiPath)) {
            return null;
        }
        SoApiService<?, ?> service = null;
        if (SoStringUtil.isNotBlank(merchantCode)) {
            service = apiServiceMap.get(generateKey(merchantCode, apiPath, KEY_CODDING));
        }
        if (service != null) {
            return service;
        }
        return apiServiceMap.get(generateKey(MERCHANT_ALL, apiPath, KEY_CODDING));
    }

    /**
     * <h2>添加接口实现类，apiPath 和 SoApiService 不能为 null</h2>
     *
     * @param merchantCode 商户编码
     * @param apiPath      接口地址
     * @param service      实现类
     */
    public void registerApiService(String merchantCode, String apiPath, SoApiService<?, ?> service) {
        if (SoStringUtil.isBlank(apiPath) || service == null) {
            return;
        }
        if (SoStringUtil.isBlank(merchantCode)) {
            merchantCode = MERCHANT_ALL;
        }
        assertApiExist(merchantCode, apiPath);
        apiServiceMap.put(generateKey(merchantCode, apiPath, KEY_CODDING), service);
    }

    /**
     * <h2>获取商户的接口配置</h2>
     *
     * @param merchantCode 商户编码
     * @param apiPath      商户路径
     * @return 商户配置
     */
    public SoMerchantNode getMerchantNode(String merchantCode, String apiPath) {
        if (SoStringUtil.isBlank(apiPath)) {
            return null;
        }
        SoMerchantNode node = null;
        if (SoStringUtil.isNotBlank(merchantCode)) {
            node = merchantConfigMap.get(generateKey(merchantCode, apiPath, KEY_XML));
        }
        if (node == null) {
            node = merchantConfigMap.get(generateKey(MERCHANT_ALL, apiPath, KEY_XML));
        }
        return node;
    }

    /**
     * <h2>添加商户接口配置</h2>
     *
     * @param merchantConfig 商户配置
     */
    public void registerMerchantNode(SoMerchantNode merchantConfig) {
        if (merchantConfig == null) {
            return;
        }

        String requestPath = merchantConfig.getRequestPath();
        if (SoStringUtil.isBlank(requestPath)) {
            return;
        }

        // 如果商户编码为空，补充全局配置文件中的商户编码
        if (SoStringUtil.isBlank(merchantConfig.getCode())) {
            merchantConfig.setCode(MERCHANT_ALL);
        }

        for (String str : requestPath.split(PATH_SPLIT_CHAR)) {
            assertApiExist(merchantConfig.getCode(), str);
            assertNodeConfig(merchantConfig);
            merchantConfigMap.put(generateKey(merchantConfig.getCode(), str, KEY_XML), merchantConfig);
        }
    }

    /**
     * <h2>注册服务节点解析器，主要用于自定义服务节点配置</h2>
     *
     * @param serviceNodeParser 自定义解析器
     * @return 是否注册成功
     */
    public static boolean registerServiceNodeParser(SoServiceNodeParser serviceNodeParser) {
        if (serviceNodeParser == null) {
            log.error("不允许将一个 null 的 SoServiceNodeParser 注册到框架中");
            return false;
        }
        return SERVICE_NODE_PARSER_MAP.add(serviceNodeParser);
    }

    /**
     * <h2>找到匹配的 node 解析器</h2>
     *
     * @param node xml 节点
     * @return 可解析该 node 的解析器
     */
    public static SoServiceNodeParser getServiceNodeParser(Node node) {
        SoServiceNodeParser parser = SERVICE_NODE_PARSER_MAP.stream()
                .filter(e -> e.isMatchedServiceParser(node))
                .findFirst()
                .orElse(null);
        if (parser != null) {
            return parser;
        }
        // 如果用户没有手动注册解析器，则从 spring 容器中查找是否有匹配的解析器
        Map<String, SoServiceNodeParser> map = SoApplicationContextAware.getBeansOfType(SoServiceNodeParser.class);
        for (Map.Entry<String, SoServiceNodeParser> entry : map.entrySet()) {
            if (entry.getValue().isMatchedServiceParser(node)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * <h2>注册服务节点执行器</h2>
     * 同一个 nodeType 的执行器多次注册会被覆盖，如果用户自定义执行器时可以直接替换默认的执行器
     *
     * @param nodeType        节点类型
     * @param execNodeService 节点执行器
     */
    public void registerServiceNodeExecutor(SoServiceNodeType nodeType, SoServiceNodeExecutor<?> execNodeService) {
        if (nodeType == null || execNodeService == null) {
            return;
        }
        if (serviceNodeExecutorMap.containsKey(nodeType)) {
            log.error("已经存在节点 [" + nodeType.getServiceNodeType() +
                    "] 对应的执行器 [" + serviceNodeExecutorMap.get(nodeType).getClass().getName() + "], 多次注册将会覆盖原执行器");
        }
        serviceNodeExecutorMap.put(nodeType, execNodeService);
    }

    /**
     * <h2>根据服务节点类型查找节点执行器</h2>
     *
     * @param serviceNodeType 服务节点类型
     * @return 未匹配到则返回 null
     */
    @SuppressWarnings("rawtypes")
    public SoServiceNodeExecutor<?> getServiceNodeExecutor(SoServiceNodeType serviceNodeType) {
        SoServiceNodeExecutor<?> executor = serviceNodeExecutorMap.get(serviceNodeType);
        if (executor != null) {
            return executor;
        }
        // 如果用户没有手动注册解析器，则从 spring 容器中查找是否有匹配的执行器
        Map<String, SoServiceNodeExecutor> map = SoApplicationContextAware.getBeansOfType(SoServiceNodeExecutor.class);
        for (Map.Entry<String, SoServiceNodeExecutor> entry : map.entrySet()) {
            if (entry.getValue().isMatchedExecutor(serviceNodeType)) {
                // 缓存起来
                serviceNodeExecutorMap.put(serviceNodeType, entry.getValue());
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * <h2>对接口配置按 order 从小到大排序</h2>
     */
    private void sortApiConfigOrderAsc() {
        for (Map.Entry<String, SoMerchantNode> merchantEntry : merchantConfigMap.entrySet()) {
            if (merchantEntry.getValue().getApiConfigList() == null) {
                continue;
            }
            merchantEntry.getValue().getApiConfigList().sort(Comparator.comparingInt(SoServiceNode::getOrder));
        }
    }

    /**
     * <h3>生成配置保存的 key</h3>
     *
     * @param merchantCode 商户编码
     * @param path         请求路径
     * @return key
     */
    private String generateKey(String merchantCode, String path, String apiType) {
        String keySeparator = "::";
        return merchantCode + keySeparator + path + keySeparator + apiType;
    }

    /**
     * <h3>在项目启动时校验接口配置，如果配置错误则直接抛出异常</h3>
     *
     * @param merchantCode 商户编码
     * @param apiPath      接口路径
     */
    private void assertApiExist(String merchantCode, String apiPath) {
        SoApiService<?, ?> apiService = getApiService(merchantCode, apiPath);
        if (apiService != null) {
            throw new SoCheckException(SoCheckErrorEnum.API_PATH_HAS_EXIST_FORMAT, null, merchantCode, apiPath, apiService.getClass().getName());
        }
        SoMerchantNode merchantNode = getMerchantNode(merchantCode, apiPath);
        if (merchantNode != null) {
            throw new SoCheckException(SoCheckErrorEnum.XML_REQ_PATH_HAS_EXIST_FORMAT, null, merchantCode, apiPath);
        }
    }

    /**
     * <h3>在项目启动时校验接口配置，如果配置错误则直接抛出异常</h3>
     *
     * @param merchantNode 服务配置
     */
    private void assertNodeConfig(SoMerchantNode merchantNode) {
        if (merchantNode == null) {
            return;
        }
        List<SoServiceNode> nodeList = merchantNode.getApiConfigList();
        if (CollectionUtils.isEmpty(nodeList)) {
            return;
        }
        Set<String> idSet = new HashSet<>(nodeList.size() + 1);
        idSet.add(merchantNode.getId());
        for (SoServiceNode apiNode : nodeList) {
            if (apiNode == null) {
                continue;
            }
            if (SoStringUtil.isBlank(apiNode.getId())) {
                continue;
            }
            // id 不允许重复
            if (idSet.contains(apiNode.getId())) {
                throw new SoCheckException(SoCheckErrorEnum.XML_NODE_ID_HAS_EXIST_FORMAT, null, merchantNode.getCode(), merchantNode.getRequestPath(), apiNode.getId());
            }
            idSet.add(apiNode.getId());
        }
    }
}
