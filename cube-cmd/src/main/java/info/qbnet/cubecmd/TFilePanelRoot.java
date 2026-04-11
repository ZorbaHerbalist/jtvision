package info.qbnet.cubecmd;

import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.KeyCode;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.views.TScrollBar;

import java.io.File;
import java.util.EnumSet;

public class TFilePanelRoot extends THideView {

    private TDrive drive = null;
    protected TFileCollection collection = null;

    public TFilePanelRoot(TRect bounds, File driveFile, TScrollBar scrollBar) {
        super(bounds);
        this.setGrowModes(EnumSet.of(GrowMode.HI_X, GrowMode.HI_Y));
        this.options |= (Options.OF_SELECTABLE + Options.OF_TOP_SELECT + Options.OF_FIRST_CLICK);
        this.eventMask = 0xFFFF;

        this.drive = new TDiskDrive(driveFile, this, -1);

        collection =  drive.getDirectory();
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
                    return;
                }
            }
        }
        collection.setSelected(0);
    }

    protected void moveSelection(int delta) {
        if (collection.visibleSize() == 0) {
            return;
        }
        collection.setSelected(collection.getSelected() + delta);
        drawView();
    }

    protected void moveSelectionToStart() {
        collection.setSelected(0);
        drawView();
    }

    protected void moveSelectionToEnd() {
        collection.setSelected(collection.visibleSize() - 1);
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
            drawView();
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
                        drawView();
                    }
                    clearEvent(event);
                }
            }
        }
    }
}
