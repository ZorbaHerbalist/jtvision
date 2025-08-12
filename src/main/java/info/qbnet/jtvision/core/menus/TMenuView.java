package info.qbnet.jtvision.core.menus;

import info.qbnet.jtvision.core.constants.Command;
import info.qbnet.jtvision.core.constants.KeyCode;
import info.qbnet.jtvision.core.event.TEvent;
import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TPalette;
import info.qbnet.jtvision.core.views.TView;

public class TMenuView extends TView {

    protected TMenuView parentMenu = null;
    protected TMenu menu = null;
    protected TMenuItem current = null;

    public static final TPalette C_MENU_VIEW = new TPalette(TPalette.parseHexString("\\x02\\x03\\x04\\x05\\x06\\x07"));

    public TMenuView(TRect bounds) {
        super(bounds);
        eventMask |= TEvent.EV_BROADCAST;

        logger.debug("{} TMenuView@TMenuView(bounds={})", getLogName(), bounds);
    }

    private enum MenuAction { DO_NOTHING, DO_SELECT, DO_RETURN }

    private void nextItem() {
        current = current.next();
        if (current == null && menu != null) {
            current = menu.items();
        }
    }

    private void prevItem() {
        TMenuItem p = current;
        if (p == menu.items()) {
            p = null;
        }
        do {
            nextItem();
        } while (current.next() != p);
    }

    private void trackKey(boolean findNext) {
        if (current != null) {
            do {
                if (findNext) {
                    nextItem();
                } else {
                    prevItem();
                }
            } while (current.name() == null);
        }
    }

    private TMenuView topMenu() {
        TMenuView p = this;
        while (p.parentMenu != null) {
            p = p.parentMenu;
        }
        return p;
    }

    @Override
    public int execute() {
        boolean autoSelect = false;
        MenuAction action = MenuAction.DO_NOTHING;
        char ch;
        int result = 0;
        TMenuItem itemShown = null;
        TMenuItem p;
        TMenuView target;
        TRect r = new TRect();
        TEvent e = new TEvent();
        // TODO
        // this.mouseActive = false;

        current = menu != null ? menu.defaultItem : null;

        while (true) {
            action = MenuAction.DO_NOTHING;
            getEvent(e);
            switch (e.what) {
                case TEvent.EV_MOUSE_DOWN:
                    // TODO
                    break;
                case TEvent.EV_MOUSE_UP:
                    // TODO
                    break;
                case TEvent.EV_MOUSE_MOVE:
                    // TODO
                    break;
                case TEvent.EV_KEYDOWN:
                    switch (KeyCode.ctrlToArrow(e.key.keyCode)) {
                        case KeyCode.KB_UP:
                        case KeyCode.KB_DOWN:
                            if (size.y != 1) {
                                trackKey(KeyCode.ctrlToArrow(e.key.keyCode) == KeyCode.KB_DOWN);
                            } else {
                                if (e.key.keyCode == KeyCode.KB_DOWN) {
                                    autoSelect = true;
                                }
                            }
                            break;
                        case KeyCode.KB_LEFT:
                        case KeyCode.KB_RIGHT:
                            if (parentMenu == null) {
                                trackKey(KeyCode.ctrlToArrow(e.key.keyCode) == KeyCode.KB_RIGHT);
                            } else {
                                action = MenuAction.DO_RETURN;
                            }
                            break;
                        case KeyCode.KB_HOME:
                        case KeyCode.KB_END:
                            if (size.y != 1) {
                                if (menu != null) {
                                    current = menu.items();
                                }
                                if (e.key.keyCode == KeyCode.KB_END) {
                                    trackKey(false);
                                }
                            }
                            break;
                        case KeyCode.KB_ENTER:
                            if (size.y == 1) {
                                autoSelect = true;
                            }
                            action = MenuAction.DO_SELECT;
                            break;
                        case KeyCode.KB_ESC:
                            action = MenuAction.DO_RETURN;
                            if (parentMenu == null || parentMenu.size.y != 1) {
                                clearEvent(e);
                            }
                            break;
                        default:
                            target = this;
                            ch = KeyCode.getAltChar(e.key.keyCode);
                            if (ch == 0) {
                                // CHECK !!!
                                ch = e.key.charCode;
                            } else {
                                target = topMenu();
                            }
                            p = target.findItem(ch);
                            if (p == null) {
                                p = topMenu().hotKey(e.key.keyCode);
                                if (p != null && commandEnabled(p.command())) {
                                    result = p.command();
                                    action = MenuAction.DO_RETURN;
                                }
                            } else {
                                if (target == this) {
                                    if (size.y == 1) {
                                        autoSelect = true;
                                    }
                                    action = MenuAction.DO_SELECT;
                                    current = p;
                                } else {
                                    if (parentMenu != target || target.current != p) {
                                        action = MenuAction.DO_RETURN;
                                    }
                                }
                            }
                            break;
                    }
                    break;
                case TEvent.EV_COMMAND:
                    if (e.msg.command == Command.CM_MENU) {
                        autoSelect = false;
                        if (parentMenu != null) {
                            action = MenuAction.DO_RETURN;
                        }
                    } else {
                        action = MenuAction.DO_RETURN;
                    }
                    break;
            }

            if (itemShown != current) {
                itemShown = current;
                drawView();
            }

            if ((action == MenuAction.DO_SELECT || (action == MenuAction.DO_NOTHING && autoSelect)) && current != null) {
                if (current.name() != null) {
                    if (current.command() == 0) {
                        if ((e.what & (TEvent.EV_MOUSE_DOWN + TEvent.EV_MOUSE_MOVE)) != 0) {
                            putEvent(e);
                        }
                        getItemRect(current, r);
                        r.a.x += origin.x;
                        r.a.y = r.b.y + origin.y;
                        r.b = owner.getSize();
                        if (size.y == 1) {
                            r.a.x--;
                        }
                        target = topMenu().newSubMenu(r, current.subMenu(), this);
                        result = owner.execView(target);
                        // assume disposal handled elsewhere
                    } else if (action == MenuAction.DO_SELECT) {
                        result = current.command();
                    }
                }
            }

            if (result != 0 && commandEnabled(result)) {
                action = MenuAction.DO_RETURN;
                clearEvent(e);
            } else {
                result = 0;
            }

            if (action == MenuAction.DO_RETURN) {
                break;
            }
        }

        if (e.what != TEvent.EV_NOTHING) {
            if (parentMenu != null || e.what == TEvent.EV_COMMAND) {
                putEvent(e);
            }
        }

        if (current != null) {
            if (menu != null) {
                menu.defaultItem = current;
            }
            current = null;
            drawView();
        }

        return result;
    }

    public TMenuItem findItem(char ch) {
        char c = Character.toUpperCase(ch);
        TMenuItem p = menu.items();

        while (p != null) {
            if (p.name() != null && !p.disabled) {
                String name = p.name();
                int i = name.indexOf('~');
                if (i != -1 && i + 1 < name.length() && c == Character.toUpperCase(name.charAt(i + 1))) {
                    return p;
                }
            }
            p = p.next();
        }

        return null;
    }

    private TMenuItem findHotKey(TMenuItem p, int keyCode) {
        while (p != null) {
            if (p.name() != null) {
                if (p.command() == 0) {
                    TMenuItem t = findHotKey(p.subMenu().items(), keyCode);
                    if (t != null) {
                        return t;
                    }
                } else if (!p.disabled && p.keyCode() != KeyCode.KB_NO_KEY && p.keyCode() == keyCode) {
                    return p;
                }
            }
            p = p.next();
        }
        return null;
    }

    public TMenuItem hotKey(int keyCode) {
        return findHotKey(menu.items(), keyCode);
    }

    @Override
    public int getHelpCtx() {
        TMenuView c = this;
        while (c != null && ((c.current == null) ||
                (c.current.helpCtx() == HelpContext.HC_NO_CONTEXT) || (c.current.name() == null))) {
            c = c.parentMenu;
        }
        if (c != null) {
            return c.current.helpCtx();
        } else {
            return HelpContext.HC_NO_CONTEXT;
        }
    }

    public void getItemRect(TMenuItem item, TRect rect) {
    }

    @Override
    public TPalette getPalette() {
        return C_MENU_VIEW;
    }

    private void doSelect(TEvent event) {
        putEvent(event);
        event.msg.command = owner.execView(this);
        if (event.msg.command != 0 && commandEnabled(event.msg.command)) {
            event.what = TEvent.EV_COMMAND;
            event.msg.infoPtr = null;
            putEvent(event);
        }
        clearEvent(event);
    }

    private boolean updateMenu(TMenu menu) {
        boolean callDraw = false;
        for (TMenuItem p = menu.items; p != null; p = p.next()) {
            if (p.name() != null) {
                if (p.command() == 0) {
                    callDraw |= updateMenu(p.subMenu());
                } else {
                    boolean commandState = commandEnabled(p.command());
                    if (p.disabled == commandState) {
                        p.disabled = !commandState;
                        callDraw = true;
                    }
                }
            }
        }
        return callDraw;
    }

    @Override
    public void handleEvent(TEvent event) {
        if (menu != null) {
            switch (event.what) {
                case TEvent.EV_MOUSE_DOWN:
                    doSelect(event);
                    break;
                case TEvent.EV_KEYDOWN:
                    if (findItem(KeyCode.getAltChar(event.key.keyCode)) != null) {
                        doSelect(event);
                    } else {
                        TMenuItem p = hotKey(event.key.keyCode);
                        if (p != null && commandEnabled(p.command)) {
                            event.what = TEvent.EV_COMMAND;
                            event.msg.command = p.command;
                            event.msg.infoPtr = null;
                            putEvent(event);
                            clearEvent(event);
                        }
                    }
                    break;
                case TEvent.EV_COMMAND:
                    if (event.msg.command == Command.CM_MENU) {
                        doSelect(event);
                    }
                    break;
                case TEvent.EV_BROADCAST:
                    if (event.msg.command == Command.CM_COMMAND_SET_CHANGED) {
                        if (updateMenu(menu)) {
                            drawView();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public TMenuView newSubMenu(TRect bounds, TMenu menu, TMenuView parentMenu) {
        // TODO
        return null;
    }

}
