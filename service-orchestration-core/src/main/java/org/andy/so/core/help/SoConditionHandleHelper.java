package org.andy.so.core.help;

import org.andy.so.core.schema.node.SoServiceNode;
import org.andy.so.core.service.SoExecNodeServiceData;

import java.util.Map;

/**
 * <h2>条件处理帮助类</h2>
 *
 * @author: andy
 */
public interface SoConditionHandleHelper {

    /**
     * <h3>执行条件处理器</h3>
     *
     * @param apiConfig         condition 节点
     * @param defaultData       默认数据
     * @param apiServiceDataMap 全量数据
     * @return 条件是否通过 {@code true} 通过，{@code false} 则未通过
     */
    Boolean handleCondition(SoServiceNode apiConfig,
                            SoExecNodeServiceData defaultData,
                            Map<String, SoExecNodeServiceData> apiServiceDataMap);
}
