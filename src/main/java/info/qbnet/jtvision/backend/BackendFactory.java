package info.qbnet.jtvision.backend;

/**
 * Factory interface for creating rendering backends.
 */
public interface BackendFactory {

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
