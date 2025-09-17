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
        FRAME_PASSIVE(1, 0x20),
        /** Active frame. */
        FRAME_ACTIVE(2, 0x21),
        /** Frame icon. */
        FRAME_ICON(3, 0x22),
        /** Scrollbar page area. */
        SCROLLBAR_PAGE(4, 0x23),
        /** Scrollbar controls. */
        SCROLLBAR_CONTROLS(5, 0x24),
        /** Static text. */
        STATIC_TEXT(6, 0x25),
        /** Normal label text. */
        LABEL_NORMAL(7, 0x26),
        /** Selected label text. */
        LABEL_SELECTED(8, 0x27),
        /** Label shortcut character. */
        LABEL_SHORTCUT(9, 0x28),
        /** Normal button text. */
        BUTTON_NORMAL(10, 0x29),
        /** Default button text. */
        BUTTON_DEFAULT(11, 0x2A),
        /** Selected button text. */
        BUTTON_SELECTED(12, 0x2B),
        /** Disabled button text. */
        BUTTON_DISABLED(13, 0x2C),
        /** Button shortcut character. */
        BUTTON_SHORTCUT(14, 0x2D),
        /** Button shadow. */
        BUTTON_SHADOW(15, 0x2E),
        /** Cluster normal text. */
        CLUSTER_NORMAL(16, 0x2F),
        /** Cluster selected text. */
        CLUSTER_SELECTED(17, 0x30),
        /** Cluster shortcut character. */
        CLUSTER_SHORTCUT(18, 0x31),
        /** Input line normal text. */
        INPUT_LINE_NORMAL_TEXT(19, 0x32),
        /** Input line selected text. */
        INPUT_LINE_SELECTED_TEXT(20, 0x33),
        /** Input line arrows. */
        INPUT_LINE_ARROWS(21, 0x34),
        /** History arrow. */
        HISTORY_ARROW(22, 0x35),
        /** History sides. */
        HISTORY_SIDES(23, 0x36),
        /** HistoryWindow scrollbar page area */
        HISTORY_WINDOW_SCROLLBAR_PAGE_AREA(24, 0x37),
        /** HistoryWindow scrollbar controls */
        HISTORY_WINDOW_SCROLLBAR_CONTROLS(25, 0x38),
        /** ListViewer normal */
        LIST_VIEWER_NORMAL(26, 0x39),
        /** ListViewer focused */
        LIST_VIEWER_FOCUSED(27, 0x3A),
        /** ListViewer selected */
        LIST_VIEWER_SELECTED(28, 0x3B),
        /** ListViewer divider */
        LIST_VIEWER_DIVIDER(29, 0x3C),
        /** InfoPane */
        INFO_PANE(30, 0x3D),
        /** Cluster disabled */
        CLUSTER_DISABLED(31, 0x3E),

        /** Reserved slot. */
        RESERVED(32, 0x3F);

        private final int index;
        private final byte grayDefault;

        DialogColor(int index, int grayDefault) {
            this.index = index;
            this.grayDefault = PaletteRole.toByte(grayDefault);
        }

        @Override
        public int index() {
            return index;
        }

        @Override
        public byte defaultValue() {
            return grayDefault;
        }

        public byte blueDefault() {
            int value = Byte.toUnsignedInt(grayDefault) + 0x20;
            return PaletteRole.toByte(value);
        }

        public byte cyanDefault() {
            int value = Byte.toUnsignedInt(grayDefault) + 0x40;
            return PaletteRole.toByte(value);
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

    public static final PaletteDescriptor<DialogColor> GRAY_DIALOG_PALETTE =
            PaletteDescriptor.register("dialog.gray", DialogColor.class);
    public static final PaletteDescriptor<DialogColor> BLUE_DIALOG_PALETTE =
            PaletteDescriptor.register("dialog.blue", DialogColor.class, DialogColor::blueDefault);
    public static final PaletteDescriptor<DialogColor> CYAN_DIALOG_PALETTE =
            PaletteDescriptor.register("dialog.cyan", DialogColor.class, DialogColor::cyanDefault);

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
    public boolean valid(int command) {
        if (command == Command.CM_CANCEL) {
            return true;
        } else {
            return super.valid(command);
        }
    }
}

