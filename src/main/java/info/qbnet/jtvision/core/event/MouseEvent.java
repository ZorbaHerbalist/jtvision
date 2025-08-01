package info.qbnet.jtvision.core.event;

import info.qbnet.jtvision.core.objects.TPoint;

public final class MouseEvent implements Event {

    private final int buttons;
    private final boolean doubleClick;
    private TPoint where;

    public MouseEvent(int buttons, boolean doubleClick, TPoint where) {
        this.buttons = buttons;
        this.doubleClick = doubleClick;
        this.where = where;
    }

    public int getButtons() {
        return buttons;
    }

    public boolean isDoubleClick() {
        return doubleClick;
    }

    public TPoint getWhere() {
        return where;
    }

    @Override
    public EventType getType() {
        return EventType.EV_MOUSE;
    }

}
