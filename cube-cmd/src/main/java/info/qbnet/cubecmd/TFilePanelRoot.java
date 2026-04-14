package info.qbnet.cubecmd;

import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.Command;
import info.qbnet.jtvision.util.KeyCode;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.views.TScrollBar;

import java.io.File;
import java.util.EnumSet;

public class TFilePanelRoot extends THideView {

    private TDrive drive = null;
    protected final TScrollBar scrollBar;
    protected TFileCollection collection = null;
    protected int topIndex;

    public TFilePanelRoot(TRect bounds, File driveFile, TScrollBar scrollBar) {
        super(bounds);
        this.setGrowModes(EnumSet.of(GrowMode.HI_X, GrowMode.HI_Y));
        this.options |= (Options.OF_SELECTABLE + Options.OF_TOP_SELECT + Options.OF_FIRST_CLICK);
        this.eventMask = 0xFFFF | TEvent.EV_BROADCAST;

        this.scrollBar = scrollBar;
        this.topIndex = 0;
        this.drive = new TDiskDrive(driveFile, this, -1);

        collection =  drive.getDirectory();
        syncScrollBar();
    }

    protected int getRowsVisible() {
        return Math.max(0, getSize().y - 1);
    }

    protected void reloadCollection(String selectedName) {
        collection = drive.getDirectory();
        if (selectedName != null) {
            for (int i = 0; i < collection.visibleSize(); i++) {
                if (selectedName.equalsIgnoreCase(collection.visibleGet(i).getName())) {
                    collection.setSelected(i);
                    ensureSelectionVisible();
                    syncScrollBar();
                    return;
                }
            }
        }
        collection.setSelected(0);
        ensureSelectionVisible();
        syncScrollBar();
    }

    protected void moveSelection(int delta) {
        if (collection.visibleSize() == 0) {
            return;
        }
        collection.setSelected(collection.getSelected() + delta);
        ensureSelectionVisible();
        syncScrollBar();
        drawView();
    }

    protected void moveSelectionToStart() {
        collection.setSelected(0);
        ensureSelectionVisible();
        syncScrollBar();
        drawView();
    }

    protected void moveSelectionToEnd() {
        collection.setSelected(collection.visibleSize() - 1);
        ensureSelectionVisible();
        syncScrollBar();
        drawView();
    }

    protected void openSelected() {
        int idx = collection.getSelected();
        if (idx < 0 || idx >= collection.visibleSize()) {
            return;
        }

        TFileRec selected = collection.visibleGet(idx);
        String previousDirName = drive.getCurrentDirectory().getName();
        boolean changed = false;

        if ("..".equals(selected.getName())) {
            changed = drive.goToParent();
        } else if (selected.isDirectory()) {
            changed = drive.enterDirectory(selected);
        }

        if (changed) {
            reloadCollection(previousDirName);
            redrawWithHeader();
        }
    }

    @Override
    public void changeBounds(TRect bounds) {
        super.changeBounds(bounds);
        ensureSelectionVisible();
        syncScrollBar();
    }

    @Override
    public void setState(int state, boolean enable) {
        super.setState(state, enable);
        if ((state & (State.SF_SELECTED | State.SF_ACTIVE | State.SF_VISIBLE)) != 0) {
            showSBar();
            drawView();
        }
    }

    private void showSBar() {
        if (scrollBar != null) {
            if (getState(State.SF_ACTIVE) && getState(State.SF_VISIBLE) && getState(State.SF_SELECTED)) {
                scrollBar.show();
            } else {
                scrollBar.hide();
            }
        }
    }

    protected void ensureSelectionVisible() {
        int selected = collection.getSelected();
        int rows = Math.max(1, getRowsVisible());
        if (selected < 0) {
            topIndex = 0;
            return;
        }
        if (selected < topIndex) {
            topIndex = selected;
        } else if (selected >= topIndex + rows) {
            topIndex = selected - rows + 1;
        }
        int maxTop = Math.max(0, collection.visibleSize() - rows);
        if (topIndex > maxTop) {
            topIndex = maxTop;
        }
        if (topIndex < 0) {
            topIndex = 0;
        }
    }

    protected void syncScrollBar() {
        if (scrollBar == null) {
            return;
        }
        int selected = Math.max(0, collection.getSelected());
        int max = Math.max(0, collection.visibleSize() - 1);
        int pageStep = Math.max(1, getRowsVisible());
        scrollBar.setParams(selected, 0, max, pageStep, 1);
    }

    protected int getTopIndex() {
        return topIndex;
    }

    public String getDirectoryName() {
        return drive.getCurrentDirectory().getPath();
    }

    protected void cycleDriveRoot() {
        File currentDirectory = drive.getCurrentDirectory();
        File currentRoot = currentDirectory;
        while (currentRoot.getParentFile() != null) {
            currentRoot = currentRoot.getParentFile();
        }

        File[] roots = File.listRoots();
        if (roots == null || roots.length == 0) {
            return;
        }

        int currentIndex = 0;
        for (int i = 0; i < roots.length; i++) {
            if (roots[i].equals(currentRoot)) {
                currentIndex = i;
                break;
            }
        }

        File nextRoot = roots[(currentIndex + 1) % roots.length];
        this.drive = new TDiskDrive(nextRoot, this, -1);
        reloadCollection(null);
        redrawWithHeader();
    }

    private void redrawWithHeader() {
        drawView();
        if (getOwner() != null) {
            getOwner().drawView();
        }
    }

    @Override
    public void handleEvent(TEvent event) {
        super.handleEvent(event);
        if (event.what == TEvent.EV_KEYDOWN) {
            switch (event.key.keyCode) {
                case KeyCode.KB_UP -> {
                    moveSelection(-1);
                    clearEvent(event);
                }
                case KeyCode.KB_DOWN -> {
                    moveSelection(1);
                    clearEvent(event);
                }
                case KeyCode.KB_PAGE_UP -> {
                    moveSelection(-Math.max(1, getRowsVisible()));
                    clearEvent(event);
                }
                case KeyCode.KB_PAGE_DOWN -> {
                    moveSelection(Math.max(1, getRowsVisible()));
                    clearEvent(event);
                }
                case KeyCode.KB_HOME -> {
                    moveSelectionToStart();
                    clearEvent(event);
                }
                case KeyCode.KB_END -> {
                    moveSelectionToEnd();
                    clearEvent(event);
                }
                case KeyCode.KB_ENTER -> {
                    openSelected();
                    clearEvent(event);
                }
                case KeyCode.KB_BACK -> {
                    if (drive.goToParent()) {
                        reloadCollection(null);
                        redrawWithHeader();
                    }
                    clearEvent(event);
                }
            }
        } else if (event.what == TEvent.EV_BROADCAST) {
            if ((options & Options.OF_SELECTABLE) != 0) {
                if (event.msg.command == Command.CM_SCROLLBAR_CLICKED && event.msg.infoPtr == scrollBar) {
                    select();
                } else if (event.msg.command == Command.CM_SCROLLBAR_CHANGED && event.msg.infoPtr == scrollBar) {
                    collection.setSelected(scrollBar.getValue());
                    ensureSelectionVisible();
                    drawView();
                }
            }
        } else if (event.what == TEvent.EV_COMMAND) {
            if (event.msg.command == TTopView.CM_CUBE_CHANGE_DRIVE) {
                cycleDriveRoot();
                clearEvent(event);
            } else if (event.msg.command == TTopView.CM_CUBE_CHANGE_DIR) {
                openSelected();
                clearEvent(event);
            }
        }
    }
}
