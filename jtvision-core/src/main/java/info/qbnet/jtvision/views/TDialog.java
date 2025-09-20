package info.qbnet.jtvision.views;

import com.fasterxml.jackson.databind.node.ObjectNode;
import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.*;

import java.io.IOException;

public class TDialog extends TWindow {

    public static final int CLASS_ID = 10;

    /**
     * Palette roles for {@link TDialog}. The indices are grouped into logical
     * sections matching the original Turbo Vision palette layout.
     */
    public enum DialogColor implements PaletteRole {
        /** Passive frame. */
        FRAME_PASSIVE,
        /** Active frame. */
        FRAME_ACTIVE,
        /** Frame icon. */
        FRAME_ICON,
        /** Scrollbar page area. */
        SCROLLBAR_PAGE,
        /** Scrollbar controls. */
        SCROLLBAR_CONTROLS,
        /** Static text. */
        STATIC_TEXT,
        /** Normal label text. */
        LABEL_NORMAL,
        /** Selected label text. */
        LABEL_SELECTED,
        /** Label shortcut character. */
        LABEL_SHORTCUT,
        /** Normal button text. */
        BUTTON_NORMAL,
        /** Default button text. */
        BUTTON_DEFAULT,
        /** Selected button text. */
        BUTTON_SELECTED,
        /** Disabled button text. */
        BUTTON_DISABLED,
        /** Button shortcut character. */
        BUTTON_SHORTCUT,
        /** Button shadow. */
        BUTTON_SHADOW,
        /** Cluster normal text. */
        CLUSTER_NORMAL,
        /** Cluster selected text. */
        CLUSTER_SELECTED,
        /** Cluster shortcut character. */
        CLUSTER_SHORTCUT,
        /** Input line normal text. */
        INPUT_LINE_NORMAL_TEXT,
        /** Input line selected text. */
        INPUT_LINE_SELECTED_TEXT,
        /** Input line arrows. */
        INPUT_LINE_ARROWS,
        /** History arrow. */
        HISTORY_ARROW,
        /** History sides. */
        HISTORY_SIDES,
        /** HistoryWindow scrollbar page area */
        HISTORY_WINDOW_SCROLLBAR_PAGE_AREA,
        /** HistoryWindow scrollbar controls */
        HISTORY_WINDOW_SCROLLBAR_CONTROLS,
        /** ListViewer normal */
        LIST_VIEWER_NORMAL,
        /** ListViewer focused */
        LIST_VIEWER_FOCUSED,
        /** ListViewer selected */
        LIST_VIEWER_SELECTED,
        /** ListViewer divider */
        LIST_VIEWER_DIVIDER,
        /** InfoPane */
        INFO_PANE,
        /** Cluster disabled */
        CLUSTER_DISABLED,

        /** Reserved slot. */
        RESERVED;
    }

    public static void registerType() {
        TStream.registerType(CLASS_ID, TDialog::new);
        JsonViewStore.registerType(TDialog.class, TDialog::new);
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

    public static final PaletteDescriptor<DialogColor> GRAY_DIALOG_PALETTE =
            PaletteDescriptor.register("dialog.gray", DialogColor.class);
    public static final PaletteDescriptor<DialogColor> BLUE_DIALOG_PALETTE =
            PaletteDescriptor.register("dialog.blue", DialogColor.class);
    public static final PaletteDescriptor<DialogColor> CYAN_DIALOG_PALETTE =
            PaletteDescriptor.register("dialog.cyan", DialogColor.class);

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

    public TDialog(ObjectNode node) {
        super(node);
        int paletteIndex = JsonUtil.getInt(node, "dialogPalette", DialogPalette.DP_GRAY_DIALOG.ordinal());
        DialogPalette[] values = DialogPalette.values();
        if (paletteIndex < 0 || paletteIndex >= values.length) {
            paletteIndex = DialogPalette.DP_GRAY_DIALOG.ordinal();
        }
        dialogPalette = values[paletteIndex];
    }

    @Override
    public TPalette getPalette() {
        switch (dialogPalette) {
            case DP_BLUE_DIALOG:
                return BLUE_DIALOG_PALETTE.palette();
            case DP_CYAN_DIALOG:
                return CYAN_DIALOG_PALETTE.palette();
            case DP_GRAY_DIALOG:
            default:
                return GRAY_DIALOG_PALETTE.palette();
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
    public void storeJson(ObjectNode node) {
        super.storeJson(node);
        node.put("dialogPalette", dialogPalette.ordinal());
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

