package info.qbnet.cubecmd;

import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.views.TScrollBar;

import java.util.EnumSet;

public class TFilePanelRoot extends THideView {

    public TFilePanelRoot(TRect bounds, int drive, TScrollBar scrollBar) {
        super(bounds);
        this.setGrowModes(EnumSet.of(GrowMode.GF_GROW_HI_X, GrowMode.GF_GROW_HI_Y));
        this.options |= (Options.OF_SELECTABLE + Options.OF_TOP_SELECT + Options.OF_FIRST_CLICK);
        this.eventMask = 0xFFFF;
    }

}
