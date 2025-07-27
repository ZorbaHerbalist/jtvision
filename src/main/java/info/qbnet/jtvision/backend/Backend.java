package info.qbnet.jtvision.backend;

/**
 * Interface for rendering backends.
 */
public interface Backend {

    /**
     * Performs post-initialization actions after the backend has been initialized by a specific library.
     * It should be called at a time appropriate for the library used to implement this class.
     */
    void afterInitialization();

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
