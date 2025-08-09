package info.qbnet.jtvision.core.menus;

import info.qbnet.jtvision.core.objects.TRect;
import info.qbnet.jtvision.core.views.TPalette;
import info.qbnet.jtvision.core.views.TView;

public class TMenuView extends TView {

    protected TMenu menu;
    protected TMenuItem current = null;

    public static final TPalette C_MENU_VIEW = new TPalette(TPalette.parseHexString("\\x02\\x03\\x04\\x05\\x06\\x07"));

    public TMenuView(TRect bounds) {
        super(bounds);

        // TODO
        logger.debug("{} TMenuView@TMenuView(bounds={})", getLogName(), bounds);
    }

    @Override
    public TPalette getPalette() {
        return C_MENU_VIEW;
    }
}
