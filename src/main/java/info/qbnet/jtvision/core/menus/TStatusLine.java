package info.qbnet.jtvision.core.menus;

import info.qbnet.jtvision.core.app.TProgram;
import info.qbnet.jtvision.core.constants.Command;
import info.qbnet.jtvision.core.event.TEvent;
import info.qbnet.jtvision.core.objects.TPoint;
import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TDrawBuffer;
import info.qbnet.jtvision.core.views.TPalette;
import info.qbnet.jtvision.core.views.TView;
import info.qbnet.jtvision.util.CString;

public class TStatusLine extends TView {

    private TStatusDef defs;
    private TStatusItem items;

    public static final TPalette C_STATUS_LINE = new TPalette(TPalette.parseHexString("\\x02\\x03\\x04\\x05\\x06\\x07"));

    public TStatusLine(TRect bounds, TStatusDef defs) {
        super(bounds);
        this.options |= Options.OF_PRE_PROCESS;
        this.eventMask |= TEvent.EV_BROADCAST;
        this.growMode = GrowMode.GF_GROW_LO_Y | GrowMode.GF_GROW_HI_X | GrowMode.GF_GROW_HI_Y;
        this.defs = defs;
        findItems();

        logger.debug("{} TStatusLine@TStatusLine(bounds={}, defs={})", getLogName(), bounds, defs);
    }

    @Override
    public void draw() {
        logger.trace("{} TStatusLine@draw()", getLogName());

        drawSelect(null);
    }

    private void drawSelect(TStatusItem selected) {
        TDrawBuffer buf = new TDrawBuffer();

        short cNormal = getColor((short) 0x0301);
        short cSelect = getColor((short) 0x0604);
        short cNormDisabled = getColor((short) 0x0202);
        short cSelDisabled = getColor((short) 0x0505);

        buf.moveChar(0, ' ', cNormal, size.x);
        TStatusItem t = items;
        int i = 0;
        while (t != null) {
            logger.trace("{} TStatusLine@drawSelect() item {}", getLogName(), t);
            if (t.text() != null) {
                int l = CString.cStrLen(t.text());
                if (i + l < size.x) {
                    short color;
                    if (commandEnabled(t.command())) {
                        if (t == selected) {
                            color = cSelect;
                        } else {
                            color = cNormal;
                        }
                    } else {
                        if (t == selected) {
                            color = cSelDisabled;
                        } else {
                            color = cNormDisabled;
                        }
                    }
                    buf.moveChar(i, ' ' , (char) color, 1);
                    buf.moveCStr(i + 1, t.text(), color);
                    buf.moveChar(i + l + 1, ' ', (char) color, 1);
                }
                i = i + l + 2;
            }
            t = t.next();
        }

        if (i < size.x - 2) {
            String hintBuf = hint(helpCtx);
            if (hintBuf != null && hintBuf.length() > 0) {
                buf.moveChar(i, (char) 179, cNormal, 1);
                i += 2;
                if (i + hintBuf.length() > size.x) {
                    hintBuf = hintBuf.substring(0, size.x - i);
                }
                buf.moveCStr(i, hintBuf, (byte) cNormal);
            }
        }

        writeLine(0, 0, size.x, 1, buf.buffer);
    }

    private void findItems() {
        TStatusDef p = defs;
        while (p != null && (helpCtx < p.min() || helpCtx > p.max())) {
            p = p.next();
        }
        if (p == null) {
            items = null;
        } else {
            items = p.items();
        }
    }

    private TStatusItem itemMouseIsIn(TPoint mouse) {
        if (mouse.y != 0) {
            return null;
        }
        int i = 0;
        TStatusItem t = items;
        while (t != null) {
            if (t.text() != null) {
                int k = i + CString.cStrLen(t.text()) + 2;
                if (mouse.x >= i && mouse.x < k) {
                    return t;
                }
                i = k;
            }
            t = t.next();
        }
        return null;
    }

    @Override
    public TPalette getPalette() {
        return C_STATUS_LINE;
    }

    @Override
    public void handleEvent(TEvent event) {
        super.handleEvent(event);
        switch (event.what) {
            case TEvent.EV_MOUSE_DOWN: {
                TStatusItem t = null;
                TPoint mouse = new TPoint();
                while (true) {
                    makeLocal(event.mouse.where, mouse);
                    TStatusItem in = itemMouseIsIn(mouse);
                    if (t != in) {
                        t = in;
                        drawSelect(t);
                    }
                    TProgram.getMouseEvent(event);
                    if (event.what != TEvent.EV_MOUSE_MOVE) {
                        break;
                    }
                }
                if (t != null && commandEnabled(t.command())) {
                    event.what = TEvent.EV_COMMAND;
                    event.msg.command = t.command();
                    event.msg.infoPtr = null;
                    putEvent(event);
                }
                clearEvent(event);
                drawView();
                break;
            }
            case TEvent.EV_KEYDOWN: {
                TStatusItem t = items;
                while (t != null) {
                    if (event.key.keyCode == t.keyCode() && commandEnabled(t.command())) {
                        event.what = TEvent.EV_COMMAND;
                        event.msg.command = t.command();
                        event.msg.infoPtr = null;
                        return;
                    }
                    t = t.next();
                }
                break;
            }
            case TEvent.EV_BROADCAST:
                if (event.msg.command == Command.CM_COMMAND_SET_CHANGED) {
                    drawView();
                }
                break;
        }
    }

    public String hint(int helpCtx) {
        return "";
    }

    /**
     * Updates the status line based on the help context of the top-most view.
     *
     * <p>If the help context has changed since the last update, this method
     * refreshes the status line's items and redraws the view.</p>
     */
    public void update() {
        TView p = topView();
        int h;
        if (p != null) {
            h = p.getHelpCtx();
        } else {
            h = HelpContext.HC_NO_CONTEXT;
        }
        if (h != helpCtx) {
            helpCtx = h;
            findItems();
            drawView();
        }
    }

}
