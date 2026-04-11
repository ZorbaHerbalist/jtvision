package info.qbnet.cubecmd;

import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.PaletteDescriptor;
import info.qbnet.jtvision.util.PaletteRole;
import info.qbnet.jtvision.util.TPalette;
import info.qbnet.jtvision.util.KeyCode;
import info.qbnet.jtvision.util.TRect;

import java.io.File;

public class TDoubleWindow extends TStdWindow {

    public enum DoubleWindowColor implements PaletteRole {
        FRAME_PASSIVE(1),
        FRAME_ACTIVE(2),
        FRAME_ICON(3),
        SCROLLBAR_PAGE(4),
        SCROLLBAR_CONTROLS(5),
        SCROLLER_NORMAL(6),
        SCROLLER_SELECTED(7),
        RESERVED(8),
        PANEL_COLOR_1(32),
        PANEL_COLOR_2(33),
        PANEL_COLOR_3(34),
        PANEL_COLOR_4(35),
        PANEL_COLOR_5(36),
        PANEL_COLOR_6(37),
        PANEL_COLOR_7(38),
        PANEL_COLOR_8(39),
        PANEL_COLOR_9(40),
        PANEL_COLOR_10(41),
        PANEL_COLOR_11(42),
        PANEL_COLOR_12(43),
        PANEL_COLOR_13(44),
        PANEL_COLOR_14(45);

        private final int index;

        DoubleWindowColor(int index) {
            this.index = index;
        }

        @Override
        public int index() {
            return index;
        }
    }

    public static final PaletteDescriptor<DoubleWindowColor> DOUBLE_WINDOW_PALETTE =
            PaletteDescriptor.register("doubleWindow", DoubleWindowColor.class);

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

        TRect separatorBounds = new TRect();
        getExtent(separatorBounds);
        separatorBounds.grow(-1, 0);
        separatorBounds.a.x = separatorBounds.b.x / 2;
        separatorBounds.b.x = separatorBounds.a.x + 2;
        separator = new TSeparator(separatorBounds, getSize().x);
        insert(separator);

        initInterior();
    }

    public void initInterior() {
        TRect leftBounds = new TRect();
        getExtent(leftBounds);
        leftBounds.grow(-1, -1);
        leftBounds.b.x = leftBounds.b.x / 2;
        initLeftView(leftBounds);

        TRect rightBounds = new TRect();
        getExtent(rightBounds);
        rightBounds.grow(-1, -1);
        rightBounds.a.x = rightBounds.b.x / 2 + 2;
        initRightView(rightBounds);

        if (leftView != null) {
            leftView.select();
        }
    }

    public void initLeftView(TRect bounds) {
        TFilePanel filePanel = new TFilePanel(bounds, leftDrive, null);
        filePanel.changeBounds(bounds);
        insert(filePanel);

        this.leftView = filePanel;
        this.leftPanel = filePanel;
    }

    public void initRightView(TRect bounds) {
        TFilePanel filePanel = new TFilePanel(bounds, rightDrive, null);
        filePanel.changeBounds(bounds);
        insert(filePanel);

        this.rightView = filePanel;
        this.rightPanel = filePanel;
    }

    private void selectOtherPanel() {
        if (leftPanel == null || rightPanel == null) {
            return;
        }
        if (leftPanel.getState(State.SF_SELECTED)) {
            rightPanel.select();
        } else {
            leftPanel.select();
        }
    }

    @Override
    public void handleEvent(TEvent event) {
        super.handleEvent(event);
        if (event.what == TEvent.EV_KEYDOWN && event.key.keyCode == KeyCode.KB_TAB) {
            selectOtherPanel();
            clearEvent(event);
        }
    }

    @Override
    public TPalette getPalette() {
        return DOUBLE_WINDOW_PALETTE.palette();
    }
}
