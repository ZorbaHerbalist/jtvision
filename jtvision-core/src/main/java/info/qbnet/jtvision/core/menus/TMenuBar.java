package info.qbnet.jtvision.core.menus;

import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.objects.TStream;
import info.qbnet.jtvision.core.views.TDrawBuffer;
import info.qbnet.jtvision.core.views.TView;
import info.qbnet.jtvision.util.CString;

public class TMenuBar extends TMenuView {

    public static final int CLASS_ID = 40;

    public static void registerType() {
        TStream.registerType(CLASS_ID, TMenuBar::new);
    }

    public static TMenuItem newItem(String name, String param, int keyCode, int command, int helpCtx, TMenuItem next) {
        if (name != null && name.length() > 0 && command != 0) {
            return new TMenuItem(next, name, command, !TView.commandEnabled(command), keyCode, helpCtx, param, null);
        }
        return next;
    }

    public static TMenuItem newLine(TMenuItem next) {
        return new TMenuItem(next, null, 0, false, 0, HelpContext.HC_NO_CONTEXT, null, null);
    }

    public static TMenuItem newSubmenu(String name, int helpCtx, TMenu subMenu, TMenuItem next) {
        return new TMenuItem(next, name, 0, false, 0, helpCtx, null, subMenu);
    }

    public static TMenu newMenu(TMenuItem items) {
        return new TMenu(items, items);
    }

    public TMenuBar(TRect bounds, TMenu menu) {
        super(bounds);
        this.growMode = GrowMode.GF_GROW_HI_X;
        this.menu = menu;
        this.options |= Options.OF_PRE_PROCESS;

        logger.debug("{} TMenuBar@TMenuBar(bounds={}, menu={})", getLogName(), bounds, menu);
    }

    public TMenuBar(TStream stream) {
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

    @Override
    public void draw() {
        logger.trace("{} TMenuBar@draw()", getLogName());

        TDrawBuffer buf = new TDrawBuffer();

        short cNormal = getColor((short) 0x0301);
        short cSelect = getColor((short) 0x0604);
        short cNormDisabled = getColor((short) 0x0202);
        short cSelDisabled = getColor((short) 0x0505);

        buf.moveChar(0, ' ', cNormal, size.x);
        if (menu != null) {
            int x = 0;
            TMenuItem p = menu.items();
            while (p != null) {
                if (p.name() != null) {
                    int l = CString.cStrLen(p.name());
                    if (x + l < size.x) {
                        short color;
                        if (p.disabled) {
                            if (p == current) {
                                color = cSelDisabled;
                            } else {
                                color = cNormDisabled;
                            }
                        } else {
                            if (p == current) {
                                color = cSelect;
                            } else {
                                color = cNormal;
                            }
                        }
                        buf.moveChar(x, ' ', color, 1);
                        buf.moveCStr(x + 1, p.name(), color);
                        buf.moveChar(x + l + 1, ' ', color, 1);
                    }
                    x = x + l + 2;
                }
                p = p.next();
            }
        }

        writeLine(0, 0, size.x, 1, buf.buffer);
    }

    @Override
    public void getItemRect(TMenuItem item, TRect rect) {
        rect.assign(1, 0, 1, 1);
        TMenuItem p = menu.items();
        while (true) {
            rect.a.x = rect.b.x;
            if (p.name() != null) {
                rect.b.x += CString.cStrLen(p.name()) + 2;
            }
            if (p == item) {
                return;
            }
            p = p.next();
        }
    }
}
