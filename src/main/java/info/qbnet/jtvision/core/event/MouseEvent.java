package info.qbnet.jtvision.core.event;

public final class MouseEvent implements Event {

    private final int buttons;
    private final boolean doubleClick;
    private final int x;
    private final int y;

    public MouseEvent(int buttons, boolean doubleClick, int x, int y) {
        this.buttons = buttons;
        this.doubleClick = doubleClick;
        this.x = x;
        this.y = y;
    }

    public int getButtons() {
        return buttons;
    }

    public boolean isDoubleClick() {
        return doubleClick;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
