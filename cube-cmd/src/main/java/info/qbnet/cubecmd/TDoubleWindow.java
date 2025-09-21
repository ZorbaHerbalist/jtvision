package info.qbnet.cubecmd;

import info.qbnet.jtvision.util.TRect;

import java.io.File;

public class TDoubleWindow extends TStdWindow {

    private File leftDrive;
    private File rightDrive;
    private THideView leftView = null;
    private THideView rightView = null;
    private TFilePanel leftPanel = null;
    private TFilePanel rightPanel = null;
    private TSeparator separator = null;

    public TDoubleWindow(TRect bounds, int number, File drive) {
        super(bounds, null, number);
        this.options |= Options.OF_TILEABLE;
        this.eventMask = 0xFFFF;
        this.leftDrive = this.rightDrive = drive;

        TRect r = new TRect();
        getExtent(r);
        r.grow(-1, 0);
        r.a.x = r.b.x / 2;
        r.b.x = r.a.x + 2;
        separator = new TSeparator(r, getSize().x);
        insert(separator);

        initInterior();

        // TODO
    }

    public void initInterior() {
        // TODO

        TRect r = new TRect();
        getExtent(r);
        r.grow(-1, -1);
        r.b.x = r.b.x / 2;
        initLeftView(r);

    }

    public void initLeftView(TRect r) {

        TFilePanel filePanel = new TFilePanel(r, leftDrive, null);

        filePanel.changeBounds(r);

        insert(filePanel);

        this.leftView = filePanel;
        this.leftPanel = filePanel;
    }

}
