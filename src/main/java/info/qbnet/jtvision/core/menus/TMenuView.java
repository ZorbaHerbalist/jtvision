package info.qbnet.jtvision.core.menus;

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

    @Override
    public TPalette getPalette() {
        return C_MENU_VIEW;
    }
}
