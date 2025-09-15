package info.qbnet.cubecmd;

import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.views.TScrollBar;

import java.io.File;
import java.util.EnumSet;

public class TFilePanelRoot extends THideView {

    private TDrive drive = null;
    protected TFileCollection collection = null;

    public TFilePanelRoot(TRect bounds, File driveFile, TScrollBar scrollBar) {
        super(bounds);
        this.setGrowModes(EnumSet.of(GrowMode.GF_GROW_HI_X, GrowMode.GF_GROW_HI_Y));
        this.options |= (Options.OF_SELECTABLE + Options.OF_TOP_SELECT + Options.OF_FIRST_CLICK);
        this.eventMask = 0xFFFF;

        this.drive = new TDiskDrive(driveFile, this, -1);

        collection =  drive.getDirectory();
    }

}
