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
    Integer getCellWidth();

    /**
     * @return height of a single character cell in pixels
     */
    Integer getCellHeight();

    /**
     * Retrieves the next pending keyboard event if one is available.
     * Implementations should return an {@link java.util.Optional#empty()} value
     * when no key has been pressed since the last call.
     *
     * @return optional key event describing the key that was pressed
     */
    java.util.Optional<info.qbnet.jtvision.core.event.KeyEvent> pollKeyEvent();

}
