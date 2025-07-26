package info.qbnet.jtvision.backend;

/**
 * Interface for rendering backends.
 */
public interface Backend {

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
