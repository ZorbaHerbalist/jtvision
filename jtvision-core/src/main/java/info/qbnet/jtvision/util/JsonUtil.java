package info.qbnet.jtvision.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Helper utilities for working with Jackson nodes when serialising and
 * deserialising Turbo Vision views to and from JSON.
 */
public final class JsonUtil {

    private JsonUtil() {
        // Utility class
    }

    public static int getInt(ObjectNode node, String field, int defaultValue) {
        if (node == null) {
            return defaultValue;
        }
        JsonNode value = node.get(field);
        return value != null && value.isInt() ? value.intValue() : defaultValue;
    }

    public static boolean getBoolean(ObjectNode node, String field, boolean defaultValue) {
        if (node == null) {
            return defaultValue;
        }
        JsonNode value = node.get(field);
        return value != null && value.isBoolean() ? value.booleanValue() : defaultValue;
    }

    public static String getString(ObjectNode node, String field) {
        if (node == null) {
            return null;
        }
        JsonNode value = node.get(field);
        return value != null && !value.isNull() ? value.asText() : null;
    }

    public static ObjectNode putPoint(ObjectNode node, String field, TPoint point) {
        if (node == null || point == null) {
            return null;
        }
        ObjectNode target = node.putObject(field);
        target.put("x", point.x);
        target.put("y", point.y);
        return target;
    }

    public static TPoint getPoint(ObjectNode node, String field, TPoint defaultValue) {
        if (node == null) {
            return defaultValue;
        }
        JsonNode pointNode = node.get(field);
        if (pointNode == null || !pointNode.isObject()) {
            return defaultValue;
        }
        int x = getInt((ObjectNode) pointNode, "x", defaultValue != null ? defaultValue.x : 0);
        int y = getInt((ObjectNode) pointNode, "y", defaultValue != null ? defaultValue.y : 0);
        return new TPoint(x, y);
    }

    public static ArrayNode getArray(ObjectNode node, String field) {
        if (node == null) {
            return null;
        }
        JsonNode value = node.get(field);
        return value != null && value.isArray() ? (ArrayNode) value : null;
    }
}

