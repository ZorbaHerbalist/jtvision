package info.qbnet.jtvision.views;

import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.Command;
import info.qbnet.jtvision.util.KeyCode;
import info.qbnet.jtvision.util.TDrawBuffer;
import info.qbnet.jtvision.util.TPalette;
import info.qbnet.jtvision.util.TPoint;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.util.TStream;

import java.io.IOException;
import java.util.EnumSet;

/**
 * Scroll bar widget translated from Turbo Vision's {@code TScrollBar}.
 * <p>
 * It provides vertical or horizontal scrolling depending on the supplied
 * bounds.  The behaviour and character set are chosen automatically based on
 * the orientation.
 * </p>
 */
public class TScrollBar extends TView {

    public static final int CLASS_ID = 3;

    public static void registerType() {
        TStream.registerType(CLASS_ID, TScrollBar::new);
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    /** Part codes identifying scroll bar regions. */
    public static final int SB_LEFT_ARROW  = 0;
    public static final int SB_RIGHT_ARROW = 1;
    public static final int SB_PAGE_LEFT   = 2;
    public static final int SB_PAGE_RIGHT  = 3;
    public static final int SB_UP_ARROW    = 4;
    public static final int SB_DOWN_ARROW  = 5;
    public static final int SB_PAGE_UP     = 6;
    public static final int SB_PAGE_DOWN   = 7;
    public static final int SB_INDICATOR   = 8;

    private static final char[] V_CHARS =
            {(char) 0x1E, (char) 0x1F, (char) 0xB1, (char) 0xFE, (char) 0xB2};
    private static final char[] H_CHARS =
            {(char) 0x11, (char) 0x10, (char) 0xB1, (char) 0xFE, (char) 0xB2};

    public static final TPalette C_SCROLL_BAR =
            new TPalette(TPalette.parseHexString("\\x04\\x05\\x05"));

    protected int value;
    protected int min;
    protected int max;
    protected int pgStep;
    protected int arStep;
    protected char[] chars;

    public TScrollBar(TRect bounds) {
        super(bounds);
        logger.debug("{} TScrollBar@TScrollBar(bounds={})", getLogName(), bounds);

        value = 0;
        min = 0;
        max = 0;
        pgStep = 1;
        arStep = 1;

        if (size.x == 1) {
            getGrowMode().addAll(EnumSet.of(GrowMode.GF_GROW_LO_X,
                    GrowMode.GF_GROW_HI_X, GrowMode.GF_GROW_HI_Y));
            chars = V_CHARS.clone();
        } else {
            getGrowMode().addAll(EnumSet.of(GrowMode.GF_GROW_LO_Y,
                    GrowMode.GF_GROW_HI_X, GrowMode.GF_GROW_HI_Y));
            chars = H_CHARS.clone();
        }
    }

    public TScrollBar(TStream stream) {
        super(stream);
        try {
            value = stream.readInt();
            min = stream.readInt();
            max = stream.readInt();
            pgStep = stream.readInt();
            arStep = stream.readInt();
            byte[] buf = stream.readBytes(5);
            chars = new char[5];
            for (int i = 0; i < 5; i++) {
                chars[i] = (char) (buf[i] & 0xFF);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void draw() {
        logger.trace("{} TScrollBar@draw()", getLogName());
        drawPos(getPos());
    }

    /** Draws the scroll bar using {@code pos} as the indicator position. */
    protected void drawPos(int pos) {
        logger.trace("{} TScrollBar@drawPos(pos={})", getLogName(), pos);

        int s = getScrollSize() - 1;
        TDrawBuffer b = new TDrawBuffer();
        b.moveChar(0, chars[0], getColor((short) 2), 1);
        if (max == min) {
            b.moveChar(1, chars[4], getColor((short) 1), s - 1);
        } else {
            b.moveChar(1, chars[2], getColor((short) 1), s - 1);
            b.moveChar(pos, chars[3], getColor((short) 3), 1);
        }
        b.moveChar(s, chars[1], getColor((short) 2), 1);
        writeBuf(0, 0, size.x, size.y, b.buffer);
    }

    @Override
    public TPalette getPalette() {
        return C_SCROLL_BAR;
    }

    protected int getPos() {
        int r = max - min;
        if (r == 0) {
            return 1;
        }
        long v = (long) (value - min) * (getScrollSize() - 3) + (r >> 1);
        return (int) (v / r) + 1;
    }

    // former getSize()
    protected int getScrollSize() {
        int s = (size.x == 1) ? size.y : size.x;
        return (s < 3) ? 3 : s;
    }

    @Override
    public void handleEvent(TEvent event) {
        logger.trace("{} TScrollBar@handleEvent(event={})", getLogName(), event);
        super.handleEvent(event);

        switch (event.what) {
            case TEvent.EV_MOUSE_DOWN -> {
                clicked();
                TPoint mouse = new TPoint();
                makeLocal(event.mouse.where, mouse);
                TRect extent = new TRect();
                getBounds(extent);
                extent.grow(1, 1);
                int p = getPos();
                int s = getScrollSize() - 1;
                int clickPart = getPartCode(mouse, extent, p, s);
                if (clickPart != SB_INDICATOR) {
                    do {
                        makeLocal(event.mouse.where, mouse);
                        if (getPartCode(mouse, extent, p, s) == clickPart) {
                            setValue(value + scrollStep(clickPart));
                        }
                    } while (mouseEvent(event, TEvent.EV_MOUSE_AUTO));
                } else {
                    boolean tracking;
                    int i;
                    do {
                        makeLocal(event.mouse.where, mouse);
                        tracking = extent.contains(mouse);
                        if (tracking) {
                            i = (size.x == 1) ? mouse.y : mouse.x;
                            if (i <= 0) i = 1;
                            if (i >= s) i = s - 1;
                        } else {
                            i = getPos();
                        }
                        if (i != p) {
                            drawPos(i);
                            p = i;
                        }
                    } while (mouseEvent(event, TEvent.EV_MOUSE_MOVE));
                    if (tracking && getScrollSize() > 3) {
                        int s2 = getScrollSize() - 3;
                        long v = (long) (p - 1) * (max - min) + (s2 >> 1);
                        setValue((int) (v / s2) + min);
                    }
                }
                clearEvent(event);
            }
            case TEvent.EV_KEYDOWN -> {
                if ((state & State.SF_VISIBLE) != 0) {
                    int clickPart = SB_INDICATOR;
                    int i = value;
                    if (size.y == 1) {
                        switch (KeyCode.ctrlToArrow(event.key.keyCode)) {
                            case KeyCode.KB_LEFT -> clickPart = SB_LEFT_ARROW;
                            case KeyCode.KB_RIGHT -> clickPart = SB_RIGHT_ARROW;
                            case KeyCode.KB_CTRL_LEFT -> clickPart = SB_PAGE_LEFT;
                            case KeyCode.KB_CTRL_RIGHT -> clickPart = SB_PAGE_RIGHT;
                            case KeyCode.KB_HOME -> i = min;
                            case KeyCode.KB_END -> i = max;
                            default -> {
                                return;
                            }
                        }
                    } else {
                        switch (KeyCode.ctrlToArrow(event.key.keyCode)) {
                            case KeyCode.KB_UP -> clickPart = SB_UP_ARROW;
                            case KeyCode.KB_DOWN -> clickPart = SB_DOWN_ARROW;
                            case KeyCode.KB_PAGE_UP -> clickPart = SB_PAGE_UP;
                            case KeyCode.KB_PAGE_DOWN -> clickPart = SB_PAGE_DOWN;
                            case KeyCode.KB_CTRL_PAGE_UP -> i = min;
                            case KeyCode.KB_CTRL_PAGE_DOWN -> i = max;
                            default -> {
                                return;
                            }
                        }
                    }
                    clicked();
                    if (clickPart != SB_INDICATOR) {
                        i = value + scrollStep(clickPart);
                    }
                    setValue(i);
                    clearEvent(event);
                }
            }
        }
    }

    private int getPartCode(TPoint mouse, TRect extent, int p, int s) {
        int part = -1;
        if (extent.contains(mouse)) {
            int mark = (size.x == 1) ? mouse.y : mouse.x;
            if (mark == p) {
                part = SB_INDICATOR;
            } else {
                if (mark < 1) part = SB_LEFT_ARROW;
                else if (mark < p) part = SB_PAGE_LEFT;
                else if (mark < s) part = SB_PAGE_RIGHT;
                else part = SB_RIGHT_ARROW;
                if (size.x == 1) {
                    part += 4;
                }
            }
        }
        return part;
    }

    private void clicked() {
        message(owner, TEvent.EV_BROADCAST, Command.CM_SCROLLBAR_CLICKED, this);
    }

    private void scrollDraw() {
        message(owner, TEvent.EV_BROADCAST, Command.CM_SCROLLBAR_CHANGED, this);
    }

    private int scrollStep(int part) {
        int step = ((part & 2) == 0) ? arStep : pgStep;
        return ((part & 1) == 0) ? -step : step;
    }

    public void setParams(int aValue, int aMin, int aMax, int aPgStep, int aArStep) {
        if (aMax < aMin) {
            aMax = aMin;
        }
        if (aValue < aMin) {
            aValue = aMin;
        }
        if (aValue > aMax) {
            aValue = aMax;
        }

        int sValue = value;
        if (sValue != aValue || min != aMin || max != aMax) {
            value = aValue;
            min = aMin;
            max = aMax;
            drawView();
            if (sValue != aValue) {
                scrollDraw();
            }
        }
        pgStep = aPgStep;
        arStep = aArStep;
    }

    public void setRange(int aMin, int aMax) {
        setParams(value, aMin, aMax, pgStep, arStep);
    }

    public void setStep(int aPgStep, int aArStep) {
        setParams(value, min, max, aPgStep, aArStep);
    }

    public void setValue(int aValue) {
        setParams(aValue, min, max, pgStep, arStep);
    }

    @Override
    public void store(TStream stream) {
        super.store(stream);
        try {
            stream.writeInt(value);
            stream.writeInt(min);
            stream.writeInt(max);
            stream.writeInt(pgStep);
            stream.writeInt(arStep);
            byte[] buf = new byte[5];
            for (int i = 0; i < 5; i++) {
                buf[i] = (byte) chars[i];
            }
            stream.writeBytes(buf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

