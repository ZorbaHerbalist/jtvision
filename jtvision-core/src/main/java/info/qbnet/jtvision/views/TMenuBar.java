package info.qbnet.jtvision.views;

import info.qbnet.jtvision.util.TMenu;
import info.qbnet.jtvision.util.TMenuItem;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.util.TStream;
import info.qbnet.jtvision.util.TDrawBuffer;
import info.qbnet.jtvision.util.CString;

import java.util.function.Consumer;

public class TMenuBar extends TMenuView {

    public static final int CLASS_ID = 40;

    public static void registerType() {
        TStream.registerType(CLASS_ID, TMenuBar::new);
    }

    public static MenuBuilder menu() {
        return new MenuBuilder();
    }

    public static final class MenuBuilder {
        private TMenuItem first;
        private TMenuItem last;

        private void append(TMenuItem item) {
            if (first == null) {
                first = item;
            } else {
                last.next = item;
            }
            last = item;
        }

        public MenuBuilder item(String name, String param, int keyCode, int command, int helpCtx) {
            if (name != null && name.length() > 0 && command != 0) {
                append(new TMenuItem(null, name, command, !TView.commandEnabled(command), keyCode, helpCtx, param, null));
            }
            return this;
        }

        public MenuBuilder separator() {
            append(new TMenuItem(null, null, 0, false, 0, HelpContext.HC_NO_CONTEXT, null, null));
            return this;
        }

        public MenuBuilder submenu(String name, int helpCtx, Consumer<MenuBuilder> consumer) {
            MenuBuilder sub = new MenuBuilder();
            consumer.accept(sub);
            append(new TMenuItem(null, name, 0, false, 0, helpCtx, null, sub.build()));
            return this;
        }

        public TMenu build() {
            return new TMenu(first, first);
        }
    }

    public TMenuBar(TRect bounds, TMenu menu) {
        super(bounds);
        getGrowMode().add(GrowMode.GF_GROW_HI_X);
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
