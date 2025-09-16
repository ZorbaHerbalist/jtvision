package info.qbnet.jtvision.views;

import info.qbnet.jtvision.util.*;
import info.qbnet.jtvision.event.TEvent;

import java.util.EnumSet;

public class TFrame extends TView {

    /**
     * Palette roles for {@link TFrame}.
     */
    public enum FrameColor implements PaletteRole {
        /** Passive frame. */
        PASSIVE_FRAME(1, 0x01),
        /** Passive title. */
        PASSIVE_TITLE(2, 0x01),
        /** Active frame. */
        ACTIVE_FRAME(3, 0x02),
        /** Active title. */
        ACTIVE_TITLE(4, 0x02),
        /** Icons and dragging helpers. */
        ICONS(5, 0x03);

        private final int index;
        private final byte defaultValue;

        FrameColor(int index, int defaultValue) {
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

    public static final int CLASS_ID = 2;

    public static void registerType() {
        TStream.registerType(CLASS_ID, TFrame::new);
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    // Palette layout
    // 1 = Passive frame
    // 2 = Passive title
    // 3 = Active frame
    // 4 = Active title
    // 5 = Icons/Dragging
    public static final TPalette C_FRAME;

    static {
        PaletteFactory.registerDefaults("frame", FrameColor.class);
        C_FRAME = PaletteFactory.get("frame");
    }

    private static final int FM_CLOSE_CLICKED   = 0x01;
    private static final int FM_ZOOM_CLICKED    = 0x02;

    private int frameMode = 0;

    private static final byte[] INIT_FRAME = {
            0x06, 0x0A, 0x0C, 0x05, 0x00, 0x05, 0x03, 0x0A, 0x09,
            0x16, 0x1A, 0x1C, 0x15, 0x00, 0x15, 0x13, 0x1A, 0x19
    };

    private static final String FRAME_CHARS =
            "   \u00C0 \u00B3\u00DA\u00C3 \u00D9\u00C4\u00C1\u00BF\u00B4\u00C2\u00C5   \u00C8 \u00BA\u00C9\u00C7 \u00BC"
                    + "\u00CD\u00CF\u00BB\u00B6\u00D1 ";

    public TFrame(TRect bounds) {
        super(bounds);
        setGrowModes(EnumSet.of(GrowMode.GF_GROW_HI_X, GrowMode.GF_GROW_HI_Y));
        this.eventMask |= TEvent.EV_BROADCAST;
    }

    public TFrame(TStream stream) {
        super(stream);
    }

    @Override
    public void store(TStream stream) {
        super.store(stream);
    }

    private void frameLine(TDrawBuffer buf, int y, int n, byte color) {
        int width = size.x;
        if (width <= 0) {
            return;
        }

        int[] frameMask = new int[width];

        frameMask[0] = INIT_FRAME[n] & 0xFF;
        if (width > 1) {
            int middle = INIT_FRAME[n + 1] & 0xFF;
            for (int i = 1; i < width - 1; i++) {
                frameMask[i] = middle;
            }
            frameMask[width - 1] = INIT_FRAME[n + 2] & 0xFF;
        }

        if (owner != null && owner.last != null) {
            TView p = owner.last;
            int dx = width - 1;
            while (true) {
                p = p.next;
                if (p == this) {
                    break;
                }
                if ((p.options & Options.OF_FRAMED) == 0) {
                    continue;
                }
                if ((p.state & State.SF_VISIBLE) == 0) {
                    continue;
                }

                int ax = y - p.origin.y;
                int al, ah;
                if (ax >= 0) {
                    if (ax > p.size.y) {
                        continue;
                    }
                    al = 0x05;
                    ah = 0x00;
                    if (ax == p.size.y) {
                        al = 0x03;
                        ah = 0x0A;
                    }
                } else {
                    ax++;
                    if (ax != 0) {
                        continue;
                    }
                    al = 0x06;
                    ah = 0x0A;
                }

                int si = p.origin.x;
                int di = p.origin.x + p.size.x;
                if (si < 1) {
                    si = 1;
                }
                if (di > dx) {
                    di = dx;
                }
                if (si >= di) {
                    continue;
                }

                frameMask[si - 1] |= al;
                al ^= ah;
                frameMask[di] |= al;
                if (ah != 0) {
                    for (int i = si; i < di; i++) {
                        frameMask[i] |= ah;
                    }
                }
            }
        }

        for (int i = 0; i < width; i++) {
            int idx = frameMask[i] & 0x1F;
            char ch = FRAME_CHARS.charAt(idx);
            buf.buffer[i] = (short) (((color & 0xFF) << 8) | (ch & 0xFF));
        }
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
            buf.buffer[width - i] = (short) ((buf.buffer[width - i] & 0xFF00) | (((TWindow) owner).number) + 0x30);
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
                if ((frameMode & FM_CLOSE_CLICKED) == 0) {
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
                    buf.buffer[width - 4] = (short) ((buf.buffer[width - 4] & 0xFF00) | 15);
                } else if ((owner.size.x == max.x) && (owner.size.y == max.y)) {
                    buf.buffer[width - 4] = (short) ((buf.buffer[width - 4] & 0xFF00) | 18);
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

    private void dragWindow(TEvent event, boolean move) {
        TRect limits = new TRect();
        TPoint min = new TPoint();
        TPoint max = new TPoint();
        owner.owner.getExtent(limits);
        owner.sizeLimits(min, max);
        owner.dragView(event, !move);
        clearEvent(event);
    }

    @Override
    public void handleEvent(TEvent event) {
        super.handleEvent(event);

        if (event.what == TEvent.EV_MOUSE_DOWN) {
            TPoint mouse = new TPoint();
            makeLocal(event.mouse.where, mouse);
            if (mouse.y == 0) {
                if ((((TWindow) owner).flags & TWindow.WindowFlag.WF_CLOSE) != 0 &&
                        (state & State.SF_ACTIVE) != 0 && mouse.x >= 2 && mouse.x <= 4) {
                    do {
                        makeLocal(event.mouse.where, mouse);
                        if (mouse.x >= 2 && mouse.x <= 4 && mouse.y == 0) {
                            frameMode |= FM_CLOSE_CLICKED;
                        } else {
                            frameMode = 0;
                        }
                        drawView();
                    } while (mouseEvent(event, TEvent.EV_MOUSE_MOVE + TEvent.EV_MOUSE_AUTO));
                    frameMode = 0;
                    if (mouse.x >= 2 && mouse.x <= 4 && mouse.y == 0) {
                        event.what = TEvent.EV_COMMAND;
                        event.msg.command = Command.CM_CLOSE;
                        event.msg.infoPtr = owner;
                        putEvent(event);
                    }
                    clearEvent(event);
                    drawView();
                } else if ((((TWindow) owner).flags & TWindow.WindowFlag.WF_ZOOM) != 0 && (state & State.SF_ACTIVE) != 0 && (event.mouse.isDouble || (mouse.x >= size.x - 5 && mouse.x <= size.x - 3))) {
                    if (!event.mouse.isDouble) {
                        do {
                            makeLocal(event.mouse.where, mouse);
                            if (mouse.x >= size.x - 5 && mouse.x <= size.x - 3 && mouse.y == 0) {
                                frameMode |= FM_ZOOM_CLICKED;
                            } else {
                                frameMode = 0;
                            }
                            drawView();
                        } while (mouseEvent(event, TEvent.EV_MOUSE_MOVE + TEvent.EV_MOUSE_AUTO));
                    }
                    frameMode = 0;
                    if (((mouse.x >= size.x - 5 && mouse.x <= size.x - 3 && mouse.y == 0)) || event.mouse.isDouble) {
                        event.what = TEvent.EV_COMMAND;
                        event.msg.command = Command.CM_ZOOM;
                        event.msg.infoPtr = owner;
                        putEvent(event);
                    }
                    clearEvent(event);
                    drawView();
                } else if ((((TWindow) owner).flags & TWindow.WindowFlag.WF_MOVE) != 0) {
                    dragWindow(event, true);
                }
            } else if ((state & State.SF_ACTIVE) != 0 && mouse.x >= size.x - 2 && mouse.y >= size.y - 1) {
                if ((((TWindow) owner).flags & TWindow.WindowFlag.WF_GROW) != 0) {
                    dragWindow(event, false);
                }
            }
        }
    }

    @Override
    public void setState(int state, boolean enable) {
        super.setState(state, enable);
        if ((state & (State.SF_ACTIVE | State.SF_DRAGGING)) != 0) {
            drawView();
        }
    }
}
