package info.qbnet.jtvision.backend.factory;

import info.qbnet.jtvision.backend.Backend;
import info.qbnet.jtvision.core.Screen;

/**
 * Factory interface for creating rendering backends.
 */
public interface Factory<B extends Backend> {

    /**
     * Creates and returns a rendering backend for the given screen buffer.
     * @param buffer the screen buffer
     * @return a rendering backend instance
     */
    B createBackend(Screen buffer);

    /**
     * Initializes any GUI-related setup before backend creation.
     *
     * <p>Default implementation performs no action. Factories that require
     * initialization can override this method.</p>
     */
    default void initialize() {
        // no-op
    }

}
