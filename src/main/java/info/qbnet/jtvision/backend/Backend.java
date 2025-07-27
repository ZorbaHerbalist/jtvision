package info.qbnet.jtvision.backend;

/**
 * Interface for rendering backends.
 */
public interface Backend {

    /**
     * Initializes the backend for a specific implementation. This method is expected
     * to be called in a location appropriate for the library being used.
     */
    void initialize();

    /**
     * Renders the given screen buffer.
     */
    void renderScreen();

    /**
     * @return width of a single character cell in pixels
     */
    Integer getCharWidth();

    /**
     * @return height of a single character cell in pixels
     */
    Integer getCharHeight();

}
