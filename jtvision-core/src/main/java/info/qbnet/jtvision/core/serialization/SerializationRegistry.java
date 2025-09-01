package info.qbnet.jtvision.core.serialization;

/**
 * Utility class responsible for forcing the JVM to load core view classes so
 * their static initializers register serialization identifiers with
 * {@code TStream}.
 */
public final class SerializationRegistry {

    private SerializationRegistry() {
        // Utility class
    }

    private static final String[] CORE_CLASSES = {
            "info.qbnet.jtvision.core.dialogs.TDialog",
            "info.qbnet.jtvision.core.dialogs.TStaticText",
            "info.qbnet.jtvision.core.dialogs.TInputLine",
            "info.qbnet.jtvision.core.dialogs.TButton"
    };

    /**
     * Loads core dialog and view classes to trigger their static registration
     * blocks.
     */
    public static void initCoreTypes() {
        for (String className : CORE_CLASSES) {
            try {
                Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
