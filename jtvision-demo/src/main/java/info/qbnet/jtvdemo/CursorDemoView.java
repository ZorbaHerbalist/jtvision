package info.qbnet.jtvdemo;

import info.qbnet.jtvision.core.constants.KeyCode;
import info.qbnet.jtvision.core.event.TEvent;
import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TView;

/**
 * Simple view demonstrating cursor handling.
 * The cursor can be moved within the view using arrow keys.
 */
public class CursorDemoView extends TView {

    private int curX = 0;
    private int curY = 0;

    public CursorDemoView(TRect bounds) {
        super(bounds);
        options |= Options.OF_SELECTABLE | Options.OF_FRAMED;
        showCursor();
        setCursor(curX, curY);
    }

    @Override
    public void draw() {
        super.draw();
        writeStr(1, 0, "Use arrow keys to move cursor", getColor((short)0x03));
    }

    @Override
    public void handleEvent(TEvent event) {
        super.handleEvent(event);
        if (event.what == TEvent.EV_KEYDOWN) {
            switch (event.key.keyCode) {
                case KeyCode.KB_LEFT -> moveCursor(-1, 0);
                case KeyCode.KB_RIGHT -> moveCursor(1, 0);
                case KeyCode.KB_UP -> moveCursor(0, -1);
                case KeyCode.KB_DOWN -> moveCursor(0, 1);
                default -> {
                    return;
                }
            }
            clearEvent(event);
        }
    }

    private void moveCursor(int dx, int dy) {
        curX = Math.max(0, Math.min(size.x - 1, curX + dx));
        curY = Math.max(0, Math.min(size.y - 1, curY + dy));
        setCursor(curX, curY);
    }
}
