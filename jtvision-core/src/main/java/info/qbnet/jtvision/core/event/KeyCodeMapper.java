package info.qbnet.jtvision.core.event;

/**
 * Utility for applying modifier flags to key codes and deriving character
 * representations. The numeric key codes used are based on the values from
 * {@code java.awt.event.KeyEvent} to keep them stable across backends.
 */
public final class KeyCodeMapper {

    private KeyCodeMapper() {}

    /** Modifier flag for the shift key. */
    public static final int SHIFT = 1 << 8;
    /** Modifier flag for the control key. */
    public static final int CTRL  = 1 << 9;
    /** Modifier flag for the alt key. */
    public static final int ALT   = 1 << 10;

    /** Left/right variants of the modifier keys. */
    public static final int SHIFT_LEFT  = 1 << 11;
    public static final int SHIFT_RIGHT = 1 << 12;
    public static final int CTRL_LEFT   = 1 << 13;
    public static final int CTRL_RIGHT  = 1 << 14;
    public static final int ALT_LEFT    = 1 << 15;
    public static final int ALT_RIGHT   = 1 << 16;

    /**
     * Applies modifier flags to the provided key code.
     *
     * @param keyCode base key code
     * @param shift   whether shift is pressed
     * @param ctrl    whether control is pressed
     * @param alt     whether alt is pressed
     * @return key code with modifier flags ORed in
     */
    public static int applyModifiers(int keyCode, boolean shift, boolean ctrl, boolean alt) {
        if (shift) {
            keyCode |= SHIFT;
        }
        if (ctrl) {
            keyCode |= CTRL;
        }
        if (alt) {
            keyCode |= ALT;
        }
        return keyCode;
    }

    /**
     * Attempts to derive an ASCII character from the given key code.
     *
     * @param keyCode mapped key code (in unified scheme)
     * @param shift   whether the shift modifier is active
     * @return character representation or {@code 0} if not printable
     */
    public static char toChar(int keyCode, boolean shift) {
        if (keyCode >= java.awt.event.KeyEvent.VK_A && keyCode <= java.awt.event.KeyEvent.VK_Z) {
            char base = (char) keyCode;
            return shift ? base : Character.toLowerCase(base);
        }
        if (keyCode >= java.awt.event.KeyEvent.VK_0 && keyCode <= java.awt.event.KeyEvent.VK_9) {
            return (char) keyCode;
        }
        if (keyCode == java.awt.event.KeyEvent.VK_SPACE) {
            return ' ';
        }
        return 0;
    }
}
