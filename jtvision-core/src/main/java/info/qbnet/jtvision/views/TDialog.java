package info.qbnet.jtvision.views;

import info.qbnet.jtvision.util.*;
import info.qbnet.jtvision.event.TEvent;

import java.io.IOException;

public class TDialog extends TWindow {

    public static final int CLASS_ID = 10;

    /**
     * Palette roles for {@link TDialog}. The indices are grouped into logical
     * sections matching the original Turbo Vision palette layout.
     */
    public enum DialogColor implements PaletteRole {
        /** Passive frame. */
        FRAME_PASSIVE(1),
        /** Active frame. */
        FRAME_ACTIVE(2),
        /** Frame icon. */
        FRAME_ICON(3),
        /** Scrollbar page area. */
        SCROLLBAR_PAGE(4),
        /** Scrollbar controls. */
        SCROLLBAR_CONTROLS(5),
        /** Static text. */
        STATIC_TEXT(6),
        /** Normal label text. */
        LABEL_NORMAL(7),
        /** Selected label text. */
        LABEL_SELECTED(8),
        /** Label shortcut character. */
        LABEL_SHORTCUT(9),
        /** Normal button text. */
        BUTTON_NORMAL(10),
        /** Default button text. */
        BUTTON_DEFAULT(11),
        /** Selected button text. */
        BUTTON_SELECTED(12),
        /** Disabled button text. */
        BUTTON_DISABLED(13),
        /** Button shortcut character. */
        BUTTON_SHORTCUT(14),
        /** Button shadow. */
        BUTTON_SHADOW(15),
        /** Cluster normal text. */
        CLUSTER_NORMAL(16),
        /** Cluster selected text. */
        CLUSTER_SELECTED(17),
        /** Cluster shortcut character. */
        CLUSTER_SHORTCUT(18),
        /** Input line normal text. */
        INPUT_LINE_NORMAL_TEXT(19),
        /** Input line selected text. */
        INPUT_LINE_SELECTED_TEXT(20),
        /** Input line arrows. */
        INPUT_LINE_ARROWS(21),
        /** History arrow. */
        HISTORY_ARROW(22),
        /** History sides. */
        HISTORY_SIDES(23),
        /** HistoryWindow scrollbar page area */
        HISTORY_WINDOW_SCROLLBAR_PAGE_AREA(24),
        /** HistoryWindow scrollbar controls */
        HISTORY_WINDOW_SCROLLBAR_CONTROLS(25),
        /** ListViewer normal */
        LIST_VIEWER_NORMAL(26),
        /** ListViewer focused */
        LIST_VIEWER_FOCUSED(27),
        /** ListViewer selected */
        LIST_VIEWER_SELECTED(28),
        /** ListViewer divider */
        LIST_VIEWER_DIVIDER(29),
        /** InfoPane */
        INFO_PANE(30),
        /** Cluster disabled */
        CLUSTER_DISABLED(31),

        /** Reserved slot. */
        RESERVED(32);

        private final int index;

        DialogColor(int index) {
            this.index = index;
        }

        @Override
        public int index() {
            return index;
        }
    }

    public static void registerType() {
        TStream.registerType(CLASS_ID, TDialog::new);
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    public enum DialogPalette {
        DP_BLUE_DIALOG,
        DP_CYAN_DIALOG,
        DP_GRAY_DIALOG
    }

    public static final TPalette C_GRAY_DIALOG = new TPalette(
            TPalette.mapFromHexString("\\x20\\x21\\x22\\x23\\x24\\x25\\x26\\x27\\x28\\x29\\x2a\\x2b\\x2c\\x2d\\x2e\\x2f\\x30\\x31\\x32\\x33\\x34\\x35\\x36\\x37\\x38\\x39\\x3a\\x3b\\x3c\\x3d\\x3e\\x3f",
                    DialogColor.values()));
    public static final TPalette C_BLUE_DIALOG = new TPalette(
            TPalette.mapFromHexString("\\x40\\x41\\x42\\x43\\x44\\x45\\x46\\x47\\x48\\x49\\x4a\\x4b\\x4c\\x4d\\x4e\\x4f\\x50\\x51\\x52\\x53\\x54\\x55\\x56\\x57\\x58\\x59\\x5a\\x5b\\x5c\\x5d\\x5e\\x5f",
                    DialogColor.values()));
    public static final TPalette C_CYAN_DIALOG = new TPalette(
            TPalette.mapFromHexString("\\x60\\x61\\x62\\x63\\x64\\x65\\x66\\x67\\x68\\x69\\x6a\\x6b\\x6c\\x6d\\x6e\\x6f\\x70\\x71\\x72\\x73\\x74\\x75\\x76\\x77\\x78\\x79\\x7a\\x7b\\x7c\\x7d\\x7e\\x7f",
                    DialogColor.values()));

    private DialogPalette dialogPalette = DialogPalette.DP_GRAY_DIALOG;

    public TDialog(TRect bounds, String title) {
        super(bounds, title, WN_NO_NUMBER);
        clearGrowModes();
        this.flags = WindowFlag.WF_MOVE + WindowFlag.WF_CLOSE;
    }

    public TDialog(TStream stream) {
        super(stream);
        try {
            dialogPalette = DialogPalette.values()[stream.readInt()];
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TPalette getPalette() {
        switch (dialogPalette) {
            case DP_BLUE_DIALOG:
                return C_BLUE_DIALOG;
            case DP_CYAN_DIALOG:
                return C_CYAN_DIALOG;
            case DP_GRAY_DIALOG:
            default:
                return C_GRAY_DIALOG;
        }
    }

    @Override
    public void handleEvent(TEvent event) {
        super.handleEvent(event);
        switch (event.what) {
            case TEvent.EV_KEYDOWN:
                switch (event.key.keyCode) {
                    case KeyCode.KB_ESC:
                        event.what = TEvent.EV_COMMAND;
                        event.msg.command = Command.CM_CANCEL;
                        event.msg.infoPtr = null;
                        putEvent(event);
                        clearEvent(event);
                        break;
                    case KeyCode.KB_ENTER:
                        event.what = TEvent.EV_BROADCAST;
                        event.msg.command = Command.CM_DEFAULT;
                        event.msg.infoPtr = null;
                        putEvent(event);
                        clearEvent(event);
                        break;
                }
                break;
            case TEvent.EV_COMMAND:
                switch (event.msg.command) {
                    case Command.CM_OK:
                    case Command.CM_CANCEL:
                    case Command.CM_YES:
                    case Command.CM_NO:
                        if ((state & State.SF_MODAL) != 0) {
                            endModal(event.msg.command);
                            clearEvent(event);
                        }
                        break;
                }
                break;
        }
    }

    @Override
    public void store(TStream stream) {
        super.store(stream);
        try {
            stream.writeInt(dialogPalette.ordinal());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean valid(int command) {
        if (command == Command.CM_CANCEL) {
            return true;
        } else {
            return super.valid(command);
        }
    }
}

