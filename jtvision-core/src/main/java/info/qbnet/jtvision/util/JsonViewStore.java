package info.qbnet.jtvision.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import info.qbnet.jtvision.views.TView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Helper that mirrors {@link TStream} but targets JSON serialisation.
 * <p>
 * Views register JSON factories using {@link #registerType(Class, Function)}
 * and can then be loaded from or stored to JSON documents.
 */
public final class JsonViewStore {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false)
            .configure(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT, false);

    private static final Map<String, Function<ObjectNode, TView>> TYPES = new HashMap<>();

    private JsonViewStore() {
        // Utility class
    }

    public static void registerType(Class<? extends TView> type, Function<ObjectNode, TView> factory) {
        TYPES.put(type.getSimpleName(), factory);
    }

    public static void registerType(String typeName, Function<ObjectNode, TView> factory) {
        TYPES.put(typeName, factory);
    }

    public static TView load(InputStream in) throws IOException {
        JsonNode root = MAPPER.readTree(in);
        if (!(root instanceof ObjectNode)) {
            throw new IOException("Root of view JSON must be an object");
        }
        return loadView((ObjectNode) root);
    }

    public static void store(OutputStream out, TView view) throws IOException {
        ObjectNode root = storeView(view);
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(out, root);
    }

    public static ObjectNode storeView(TView view) {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("class", view.getClass().getSimpleName());
        view.storeJson(node);
        return node;
    }

    public static TView loadView(ObjectNode node) {
        String typeName = JsonUtil.getString(node, "class");
        if (typeName == null || typeName.isEmpty()) {
            throw new IllegalStateException("View JSON is missing class name");
        }
        Function<ObjectNode, TView> factory = TYPES.get(typeName);
        if (factory == null) {
            throw new IllegalStateException("Unknown view type: " + typeName);
        }
        return factory.apply(node);
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }
}

