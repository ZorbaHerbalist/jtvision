package info.qbnet.jtvision.backend.factory;

import info.qbnet.jtvision.backend.Backend;
import info.qbnet.jtvision.backend.Screen;

/**
 * Factory interface for creating rendering backends.
 */
public interface Factory {

    /**
     * Creates and returns a rendering backend for the given screen buffer.
     * @param buffer the screen buffer
     * @return a rendering backend instance
     */
    Backend createBackend(Screen buffer);

    /**
     * Initializes any GUI-related setup before backend creation.
     */
    void initialize();

}
