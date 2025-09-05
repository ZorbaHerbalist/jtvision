package info.qbnet.jtvision.views;

import info.qbnet.jtvision.util.TMenu;
import info.qbnet.jtvision.util.TMenuItem;
import info.qbnet.jtvision.util.TPoint;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.util.TStream;
import info.qbnet.jtvision.util.TDrawBuffer;
import info.qbnet.jtvision.util.CString;

public class TMenuBox extends TMenuView {

    public static final int CLASS_ID = 41;

    public static void registerType() {
        TStream.registerType(CLASS_ID, TMenuBox::new);
    }

    public TMenuBox(TRect bounds, TMenu menu, TMenuView parentMenu) {
        super(bounds);

        int w = 10;
        int h = 2;
        if (menu != null) {
            TMenuItem p = menu.items;
            while (p != null) {
                if (p.name() != null) {
                    int l = CString.cStrLen(p.name()) + 6;
                    if (p.command() == 0) {
                        l += 3;
                    } else {
                        if (p.param != null) {
                            l += CString.cStrLen(p.param) + 2;
                        }
                    }
                    if (l > w) {
                        w = l;
                    }
                }
                h++;
                p = p.next;
            }
        }

        TRect r = new TRect();
        r.copy(bounds);
        if (r.a.x + w < r.b.x) {
            r.b.x = r.a.x + w;
        } else {
            r.a.x = r.b.x - w;
        }
        if (r.a.y + h < r.b.y) {
            r.b.y = r.a.y + h;
        } else {
            r.a.y = r.b.y - h;
        }
        // init(r)
        this.origin = r.a;
        this.size = new TPoint(r.b.x - r.a.x, r.b.y - r.a.y);

        this.state |= State.SF_SHADOW;
        this.options |= Options.OF_PRE_PROCESS;
        this.menu = menu;
        this.parentMenu = parentMenu;
    }

    public TMenuBox(TStream stream) {
        super(stream);
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    @Override
    public void store(TStream stream) {
        super.store(stream);
    }

    private static final String FRAME_CHARS =
            "\u0020\u00da\u00c4\u00bf\u0020\u0020\u00c0\u00c4\u00d9\u0020" +
            "\u0020\u00b3\u0020\u00b3\u0020\u0020\u00c3\u00c4\u00b4\u0020";
//    private static final String FRAME_CHARS =
//            "\\x20\\xda\\xc4\\xbf\\x20\\x20\\xc0\\xc4\\xd9\\x20" +
//            "\\x20\\xb3\\x20\\xb3\\x20\\x20\\xc3\\xc4\\xb4\\x20";

    private void frameLine(int n, TDrawBuffer b, short cNormal, short color) {
        b.moveChar(0, FRAME_CHARS.charAt(n), (char) cNormal, 1);
        b.moveChar(1, FRAME_CHARS.charAt(n + 1), (char) cNormal, 1);
        b.moveChar(2, FRAME_CHARS.charAt(n + 2), (char) color, size.x - 4);
        b.moveChar(size.x - 2, FRAME_CHARS.charAt(n + 3), (char) cNormal, 1);
        b.moveChar(size.x - 1, FRAME_CHARS.charAt(n + 4), (char) cNormal, 1);
    }

    private void drawLine(int y, TDrawBuffer b) {
        writeBuf(0, y, size.x, 1, b.buffer);
    }

    @Override
    public void draw() {
        logger.trace("{} TMenuBox@draw()", getLogName());

        TDrawBuffer buf = new TDrawBuffer();

        short cNormal = getColor((short) 0x0301);
        short cSelect = getColor((short) 0x0604);
        short cNormDisabled = getColor((short) 0x0202);
        short cSelDisabled = getColor((short) 0x0505);

        int y = 0;

        frameLine(0, buf, cNormal, cNormal);
        drawLine(y++, buf);

        if (menu != null) {
            TMenuItem p = menu.items;
            while (p != null) {
                if (p.name == null) {
                    frameLine(15, buf, cNormal, cNormal);
                } else {
                    short color = cNormal;
                    if (p.disabled) {
                        if (p == current) {
                            color = cSelDisabled;
                        } else {
                            color = cNormDisabled;
                        }
                    } else if (p == current) {
                        color = cSelect;
                    }
                    frameLine(10, buf, cNormal, color);
                    buf.moveCStr(3, p.name(), color);
                    if (p.command() == 0) {
                        buf.moveChar(size.x - 4, (char) 16, color, 1);
                    } else if (p.param != null) {
                        buf.moveCStr(size.x - 3 - p.param.length(), p.param, color);
                    }
                }
                drawLine(y++, buf);
                p = p.next;
            }
        }

        frameLine(5, buf, cNormal, cNormal);
        drawLine(y, buf);
    }

    @Override
    public void getItemRect(TMenuItem item, TRect rect) {
        int y = 1;
        TMenuItem p = menu.items;
        while (p != item) {
            y++;
            p = p.next;
        }
        rect.assign(2, y, size.x -2, y + 1);
    }
}
