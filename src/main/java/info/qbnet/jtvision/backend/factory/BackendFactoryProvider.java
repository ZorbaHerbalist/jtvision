package info.qbnet.jtvision.backend.factory;

import info.qbnet.jtvision.backend.*;

/**
 * Utility class that provides {@link AbstractGuiFactory} instances for each
 * {@link BackendType} using a simple switch statement.
 */
public final class BackendFactoryProvider {

    private BackendFactoryProvider() {
        // utility class
    }

    /**
     * Returns a factory instance for the given backend type.
     *
     * @param type selected backend type
     * @return factory capable of creating the backend
     */
    public static AbstractGuiFactory<? extends Backend> getFactory(BackendType type) {
        return switch (type) {
            case SWING_BASIC -> new SwingFactory(SwingBasicBackend::new);
            case SWING_BITMAP -> new SwingFactory(SwingBitmapBackend::new);
            case SWING_TRUETYPE -> new SwingFactory(SwingTrueTypeBackend::new);
            case JAVAFX_BITMAP -> new JavaFxFactory(JavaFxBitmapBackend::new);
            case JAVAFX_TRUETYPE -> new JavaFxFactory(JavaFxTrueTypeBackend::new);
            case LIBGDX_BITMAP -> new LibGdxFactory(LibGdxBitmapBackend::new);
            case LIBGDX_TRUETYPE -> new LibGdxFactory(LibGdxTrueTypeBackend::new);
        };
    }
}
