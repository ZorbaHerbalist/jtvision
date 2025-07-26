package info.qbnet.jtvision.backend.factory;

import info.qbnet.jtvision.backend.*;

/**
 * Utility class that provides {@link Factory} instances for each
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
    public static Factory<? extends GuiComponent<?>> getFactory(BackendType type) {
        final int charWidth = 8;
        final int charHeight = 16;

        return switch (type) {
            case SWING_BASIC -> new SwingFactory(screen -> new SwingBasicBackend(screen, charWidth, charHeight));
            case SWING_BITMAP -> new SwingFactory(screen -> new SwingBitmapBackend(screen, charWidth, charHeight));
            case SWING_TRUETYPE -> new SwingFactory(screen -> new SwingTrueTypeBackend(screen, charWidth, charHeight));
            case JAVAFX_BITMAP -> new JavaFxFactory(screen -> new JavaFxBitmapBackend(screen, charWidth, charHeight));
            case JAVAFX_TRUETYPE -> new JavaFxFactory(screen -> new JavaFxTrueTypeBackend(screen, charWidth, charHeight));
            case LIBGDX_BITMAP -> new LibGdxFactory(screen -> new LibGdxBitmapBackend(screen, charWidth, charHeight));
            case LIBGDX_TRUETYPE -> new LibGdxFactory(screen -> new LibGdxTrueTypeBackend(screen, charWidth, charHeight));
        };
    }
}
