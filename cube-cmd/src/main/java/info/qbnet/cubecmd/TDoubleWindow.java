package info.qbnet.cubecmd;

import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.KeyCode;
import info.qbnet.jtvision.util.PaletteDescriptor;
import info.qbnet.jtvision.util.PaletteRole;
import info.qbnet.jtvision.util.TPalette;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.views.TScrollBar;

import java.io.File;

public class TDoubleWindow extends TStdWindow {
    private static final int MIN_PANEL_WIDTH = 8;

    /**
     * Palette layout matching Dos Navigator's CDoubleWindow table.
     *
     * <p>Keeping all slots allows child views (now and in future) to resolve
     * exactly the same indices as in DN.</p>
     */
    public enum DoubleWindowColor implements PaletteRole {
        FRAME_PASSIVE,
        FRAME_ACTIVE,
        FRAME_INACTIVE,
        SCROLLBAR_PAGE,
        SCROLLBAR_ARROW,
        PANEL_NORMAL_TEXT,
        PANEL_SEPARATOR,
        PANEL_SELECTED_TEXT,
        PANEL_CURSOR_NORMAL,
        PANEL_CURSOR_SELECTED,
        PANEL_TOP_ACTIVE,
        PANEL_TOP_PASSIVE,
        VIEWER_NORMAL_TEXT,
        VIEWER_SELECTED_TEXT,
        TREE_TEXT,
        TREE_NORMAL_ITEM,
        TREE_SELECTED_ITEM,
        TREE_DIRECTORY_FLAG,
        TREE_DIRECTORY_SELECTED,
        TREE_SOURCE_ITEM,
        TREE_DESTINATION_ITEM,
        TREE_INFO,
        DISK_INFO_NORMAL,
        DISK_INFO_HIGHLIGHT,
        FILE_INFO_1,
        FILE_INFO_2,
        FILE_INFO_3,
        FILE_INFO_4,
        FILE_INFO_5,
        FILE_INFO_6,
        FILE_INFO_7,
        FILE_PANEL_MARKER,
        DRIVE_LINE_1,
        DRIVE_LINE_2,
        DRIVE_LINE_3,
        DRIVE_LINE_4,
        DRIVE_LINE_5,
        DRIVE_LINE_6,
        DRIVE_LINE_7,
        DRIVE_LINE_8,
        DRIVE_LINE_9,
        DRIVE_LINE_10,
        DRIVE_LINE_11
    }

    public static final PaletteDescriptor<DoubleWindowColor> DOUBLE_WINDOW_PALETTE =
            PaletteDescriptor.register("doubleWindow", DoubleWindowColor.class);

    private File leftDrive;
    private File rightDrive;
    private THideView leftView = null;
    private THideView rightView = null;
    private TTopView leftTopView = null;
    private TTopView rightTopView = null;
    private TFilePanel leftPanel = null;
    private TFilePanel rightPanel = null;
    private TScrollBar leftScrollBar = null;
    private TScrollBar rightScrollBar = null;
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
        TRect leftBounds = computeLeftBounds();
        initLeftView(leftBounds);

        TRect rightBounds = computeRightBounds();
        initRightView(rightBounds);

        if (leftView != null) {
            leftView.select();
        }
    }

    private int getSeparatorLeftX() {
        int ownerWidth = getSize().x;
        int minX = Math.max(1, MIN_PANEL_WIDTH + 1);
        int maxX = Math.max(minX, ownerWidth - MIN_PANEL_WIDTH - 2);

        int oldW = Math.max(1, separator.getOldW());
        int scaledCenter = (int) Math.round((double) separator.getOldX() * ownerWidth / oldW);
        int separatorLeft = scaledCenter - 1;

        if (separatorLeft < minX) {
            separatorLeft = minX;
        } else if (separatorLeft > maxX) {
            separatorLeft = maxX;
        }
        return separatorLeft;
    }

    private TRect computeLeftBounds() {
        TRect leftBounds = new TRect();
        getExtent(leftBounds);
        leftBounds.grow(-1, -1);
        leftBounds.b.x = Math.max(leftBounds.a.x + 1, getSeparatorLeftX());
        return leftBounds;
    }

    private TRect computeRightBounds() {
        TRect rightBounds = new TRect();
        getExtent(rightBounds);
        rightBounds.grow(-1, -1);
        rightBounds.a.x = getSeparatorLeftX() + 2;
        return rightBounds;
    }

    private TRect computeLeftTopBounds() {
        TRect topBounds = computeLeftBounds();
        topBounds.b.y = topBounds.a.y;
        topBounds.a.y -= 1;
        return topBounds;
    }

    private TRect computeRightTopBounds() {
        TRect topBounds = computeRightBounds();
        topBounds.b.y = topBounds.a.y;
        topBounds.a.y -= 1;
        return topBounds;
    }

    private TRect computeLeftScrollBarBounds() {
        TRect leftScrollBounds = new TRect();
        getExtent(leftScrollBounds);
        leftScrollBounds.grow(-1, -1);
        int x = getSeparatorLeftX();
        leftScrollBounds.a.x = x;
        leftScrollBounds.b.x = x + 1;
        return leftScrollBounds;
    }

    private TRect computeRightScrollBarBounds() {
        TRect rightScrollBounds = new TRect();
        rightScrollBounds = computeRightBounds();
        int x = rightScrollBounds.b.x;
        rightScrollBounds.a.x = x;
        rightScrollBounds.b.x = x + 1;
        return rightScrollBounds;
    }

    private TRect computeSeparatorBounds() {
        TRect separatorBounds = new TRect();
        getExtent(separatorBounds);
        separatorBounds.grow(-1, 0);
        separatorBounds.a.x = getSeparatorLeftX();
        separatorBounds.b.x = separatorBounds.a.x + 2;
        return separatorBounds;
    }

    private void relayoutPanels() {
        if (separator == null) {
            return;
        }
        TRect separatorBounds = computeSeparatorBounds();
        TRect leftTopBounds = computeLeftTopBounds();
        TRect rightTopBounds = computeRightTopBounds();
        TRect leftBounds = computeLeftBounds();
        TRect rightBounds = computeRightBounds();
        TRect leftScrollBounds = computeLeftScrollBarBounds();
        TRect rightScrollBounds = computeRightScrollBarBounds();

        separator.locate(separatorBounds);
        if (leftScrollBar != null) {
            leftScrollBar.locate(leftScrollBounds);
        }
        if (rightScrollBar != null) {
            rightScrollBar.locate(rightScrollBounds);
        }
        if (leftTopView != null) {
            leftTopView.locate(leftTopBounds);
        }
        if (rightTopView != null) {
            rightTopView.locate(rightTopBounds);
        }
        if (leftView != null) {
            leftView.locate(leftBounds);
        }
        if (rightView != null) {
            rightView.locate(rightBounds);
        }
    }

    public void initLeftView(TRect bounds) {
        TRect topBounds = computeLeftTopBounds();
        TRect scrollBounds = computeLeftScrollBarBounds();
        leftScrollBar = new TScrollBar(scrollBounds);
        insert(leftScrollBar);

        TFilePanel filePanel = new TFilePanel(bounds, leftDrive, leftScrollBar);
        insert(filePanel);

        TTopView topView = new TTopView(topBounds, filePanel);
        insert(topView);

        this.leftTopView = topView;
        this.leftView = filePanel;
        this.leftPanel = filePanel;
    }

    public void initRightView(TRect bounds) {
        TRect topBounds = computeRightTopBounds();
        TRect scrollBounds = computeRightScrollBarBounds();
        rightScrollBar = new TScrollBar(scrollBounds);
        insert(rightScrollBar);

        TFilePanel filePanel = new TFilePanel(bounds, rightDrive, rightScrollBar);
        insert(filePanel);

        TTopView topView = new TTopView(topBounds, filePanel);
        insert(topView);

        this.rightTopView = topView;
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
    public void changeBounds(TRect bounds) {
        super.changeBounds(bounds);
        relayoutPanels();
        drawView();
    }

    @Override
    public TPalette getPalette() {
        return DOUBLE_WINDOW_PALETTE.palette();
    }
}
