package info.qbnet.jtvision.backend;

/**
 * Provides character cell dimensions in pixels.
 */
public interface CharDimensions {
    /**
     * @return width of a single character cell in pixels
     */
    Integer getCharWidth();

    /**
     * @return height of a single character cell in pixels
     */
    Integer getCharHeight();
}
