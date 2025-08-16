package info.qbnet.jtvision.core.views;

import info.qbnet.jtvision.core.event.TEvent;
import info.qbnet.jtvision.core.objects.TPoint;
import info.qbnet.jtvision.core.objects.TRect;

public class TFrame extends TView {

    public static final TPalette C_FRAME = new TPalette(TPalette.parseHexString("\\x01\\x01\\x02\\x02\\x03"));

    private static final int FM_CLOSE_CLICKED   = 0x01;
    private static final int FM_ZOOM_CLICKED    = 0x02;

    private int frameMode = 0;

    public TFrame(TRect bounds) {
        super(bounds);
        this.growMode |= GrowMode.GF_GROW_HI_X + GrowMode.GF_GROW_HI_Y;
        this.eventMask |= TEvent.EV_BROADCAST;
    }

    private void frameLine(TDrawBuffer buf, int y, int n, byte color) {
        // TODO
    }

    @Override
    public void draw() {
        logger.trace("{} TFrame@draw()", getLogName());

        TDrawBuffer buf = new TDrawBuffer();

        short cFrame;
        short cTitle;
        int f;

        if ((state & State.SF_DRAGGING) != 0) {
            cFrame = getColor((short) 0x0505);
            cTitle = getColor((short) 0x0005);
            f = 0;
        } else if ((state & State.SF_ACTIVE) == 0) {
            cFrame = getColor((short) 0x0101);
            cTitle = getColor((short) 0x0002);
            f = 0;
        } else {
            cFrame = getColor((short) 0x0503);
            cTitle = getColor((short) 0x0004);
            f = 9;
        }
        int width = size.x;
        int l = width - 10;
        if ((((TWindow) owner).flags & (TWindow.WindowFlag.WF_CLOSE + TWindow.WindowFlag.WF_ZOOM)) != 0) {
            l -= 6;
        }
        frameLine(buf, 0, f, (byte) cFrame);
        if ((((TWindow) owner).number != TWindow.WN_NO_NUMBER) && (((TWindow) owner).number < 10)) {
            l -= 4;
            int i = 3;
            if ((((TWindow) owner).flags & TWindow.WindowFlag.WF_ZOOM) != 0) {
                i = 7;
            }
            buf.buffer[width - i] = (short) ((buf.buffer[width - i] & 0xFF00) & ((((TWindow) owner).number) + 0x30));
        }
        String title = ((TWindow) owner).getTitle(l);
        if (!title.isEmpty()) {
            l = title.length();
            if (l > width - 10) {
                l = width - 10;
            }
            if (l < 0) {
                l = 0;
            }
            int i = (width - l) / 2;
            buf.moveChar(i - 1, ' ', cTitle, 1);
            buf.moveStr(i, title.substring(0, l), cTitle);
            buf.moveChar(i + l, ' ', cTitle, 1);
        }
        if ((state & State.SF_ACTIVE) != 0) {
            if ((((TWindow) owner).flags & TWindow.WindowFlag.WF_CLOSE) != 0) {
                if ((frameMode & FM_CLOSE_CLICKED) != 0) {
                    buf.moveCStr(2, "[~" + (char) 254 + "~]", cFrame);
                } else {
                    buf.moveCStr(2, "[~" + (char) 15 + "~]", cFrame);
                }
            }
            if ((((TWindow) owner).flags & TWindow.WindowFlag.WF_ZOOM) != 0) {
                buf.moveCStr(width - 5, "[~" + (char) 24 + "~]", cFrame);
                TPoint min = new TPoint();
                TPoint max = new TPoint();
                owner.sizeLimits(min, max);
                if ((frameMode & FM_ZOOM_CLICKED) != 0) {
                    buf.buffer[width - 4] = (short) ((buf.buffer[width - 4] & 0xFF00) & 15);
                } else if ((owner.size.x == max.x) && (owner.size.y == max.y)) {
                    buf.buffer[width - 4] = (short) ((buf.buffer[width - 4] & 0xFF00) & 18);
                }
            }
        }
        writeLine(0,0,size.x, 1, buf.buffer);

        for (int i = 1; i <= size.y - 2; i++) {
            frameLine(buf, i, f + 3, (byte) cFrame);
            writeLine(0, i, size.x, 1, buf.buffer);
        }

        frameLine(buf, size.y - 1, f + 6, (byte) cFrame);
        if ((state & State.SF_ACTIVE) != 0) {
            if ((((TWindow) owner).flags & TWindow.WindowFlag.WF_GROW) != 0) {
                buf.moveCStr(width - 2, "~" + (char) 196 + (char) 217 + "~" , cFrame);
            }
        }
        writeLine(0, size.y - 1, size.x, 1, buf.buffer);
    }

    @Override
    public TPalette getPalette() {
        return C_FRAME;
    }

    @Override
    public void setState(int state, boolean enable) {
        super.setState(state, enable);
        if ((state & (State.SF_ACTIVE | State.SF_DRAGGING)) != 0) {
            drawView();
        }
    }
}
