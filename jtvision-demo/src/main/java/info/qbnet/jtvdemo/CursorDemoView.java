package info.qbnet.jtvdemo;

import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.KeyCode;
import info.qbnet.jtvision.util.PaletteRole;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.views.TView;

/**
 * Simple view demonstrating cursor handling.
 * The cursor can be moved within the view using arrow keys and
 * the cursor shape can be switched between underline and block styles.
 */
public class CursorDemoView extends TView {

    private enum CursorDemoColor implements PaletteRole {
        TEXT(3, 0x03);

        private final int index;
        private final byte defaultValue;

        CursorDemoColor(int index, int defaultValue) {
            this.index = index;
            this.defaultValue = PaletteRole.toByte(defaultValue);
        }

        @Override
        public int index() {
            return index;
        }

        @Override
        public byte defaultValue() {
            return defaultValue;
        }
    }

    private int curX = 0;
    private int curY = 0;
    /** Indicates whether the cursor is in block mode. */
    private boolean blockMode = false;

    public CursorDemoView(TRect bounds) {
        super(bounds);
        options |= Options.OF_SELECTABLE | Options.OF_FRAMED;
        showCursor();
        setCursor(curX, curY);
    }

    @Override
    public void draw() {
        super.draw();
        writeStr(1, 0, "Use arrow keys to move cursor", getColor(CursorDemoColor.TEXT));
        writeStr(1, 1, "B - block cursor, U - underline", getColor(CursorDemoColor.TEXT));
        String mode = blockMode ? "BLOCK" : "UNDERLINE";
        writeStr(1, 2, "Current: " + mode, getColor(CursorDemoColor.TEXT));
    }

    @Override
    public void handleEvent(TEvent event) {
        super.handleEvent(event);
        if (event.what == TEvent.EV_KEYDOWN) {
            boolean handled = true;
            switch (event.key.keyCode) {
                case KeyCode.KB_LEFT -> moveCursor(-1, 0);
                case KeyCode.KB_RIGHT -> moveCursor(1, 0);
                case KeyCode.KB_UP -> moveCursor(0, -1);
                case KeyCode.KB_DOWN -> moveCursor(0, 1);
                default -> {
                    handled = handleCharKey(event.key.charCode);
                }
            }
            if (handled) {
                clearEvent(event);
            }
        }
    }

    /**
     * Handles character keys for toggling the cursor type.
     *
     * @param ch the character that was pressed
     * @return {@code true} if the key was processed
     */
    private boolean handleCharKey(char ch) {
        ch = Character.toUpperCase(ch);
        switch (ch) {
            case 'B' -> {
                blockCursor();
                blockMode = true;
                drawView();
                return true;
            }
            case 'U' -> {
                normalCursor();
                blockMode = false;
                drawView();
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private void moveCursor(int dx, int dy) {
        curX = Math.max(0, Math.min(size.x - 1, curX + dx));
        curY = Math.max(0, Math.min(size.y - 1, curY + dy));
        setCursor(curX, curY);
    }
}
