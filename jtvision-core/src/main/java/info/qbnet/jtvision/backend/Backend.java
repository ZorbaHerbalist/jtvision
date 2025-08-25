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
     * Returns a bit mask of currently pressed mouse buttons. Bit {@code 0}
     * represents the left button and bit {@code 1} the right button.
     *
     * <p>The returned coordinates are already translated to console cell
     * coordinates by the backend implementation.</p>
     *
     * @return mask of pressed mouse buttons
     */
    int getMouseButtons();

    /**
     * Returns the current mouse cursor location in character cell
     * coordinates.
     *
     * @return current cursor position
     */
    info.qbnet.jtvision.core.objects.TPoint getMouseLocation();

    /**
     * Retrieves the next pending event if one is available. Implementations
     * should return an {@link java.util.Optional#empty()} value when no input
     * has been received since the last call.
     *
     * @return optional event describing the input that occurred
     */
    java.util.Optional<info.qbnet.jtvision.core.event.TEvent> pollEvent();

    /**
     * Returns the current state of keyboard modifier keys.
     *
     * <p>The bits of the returned byte correspond to the following modifiers:
     * <ul>
     *   <li>Bit {@code 0} – Right Shift</li>
     *   <li>Bit {@code 1} – Left Shift</li>
     *   <li>Bit {@code 2} – Control</li>
     *   <li>Bit {@code 3} – Alt</li>
     * </ul>
     * Multiple bits may be set if several modifiers are pressed
     * simultaneously.</p>
     *
     * @return bit mask describing the current modifier state
     */
     byte getShiftState();

}
