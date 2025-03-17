package org.andy.so.core.schema.node;

/**
 * <h2>配置抽象类，预留，用于扩展</h2>
 *
 * @author: andy
 */
public abstract class SoAbstractNode {
    /**
     * 配置唯一 ID
     */
    protected String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "AbstractNode{" +
                "id='" + id + '\'' +
                '}';
    }
}
