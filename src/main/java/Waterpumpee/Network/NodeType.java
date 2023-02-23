package Waterpumpee.Network;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public enum NodeType implements Serializable {
    INPUT("input"),
    OUTPUT("output"),
    HIDDEN("hidden");

    private String label;
    private static final Map<String, NodeType> lookup = new HashMap<>();

    NodeType(String label) {
        this.label = label;
    }

    static {
        for (NodeType type : NodeType.values()) {
            lookup.put(type.getLabel(), type);
        }
    }

    public String getLabel() {
        return label;
    }

    public static NodeType get(String label) {
        return lookup.get(label);
    }
}