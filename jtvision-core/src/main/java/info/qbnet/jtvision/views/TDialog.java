package info.qbnet.jtvision.views;

import info.qbnet.jtvision.util.Command;
import info.qbnet.jtvision.util.KeyCode;
import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.util.TStream;
import info.qbnet.jtvision.util.TPalette;

import java.io.IOException;

public class TDialog extends TWindow {

    public static final int CLASS_ID = 10;

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

    public static final TPalette C_GRAY_DIALOG = new TPalette(TPalette.parseHexString("\\x20\\x21\\x22\\x23\\x24\\x25\\x26\\x27\\x28\\x29\\x2a\\x2b\\x2c\\x2d\\x2e\\x2f\\x30\\x31\\x32\\x33\\x34\\x35\\x36\\x37\\x38\\x39\\x3a\\x3b\\x3c\\x3d\\x3e\\x3f"));
    public static final TPalette C_BLUE_DIALOG = new TPalette(TPalette.parseHexString("\\x40\\x41\\x42\\x43\\x44\\x45\\x46\\x47\\x48\\x49\\x4a\\x4b\\x4c\\x4d\\x4e\\x4f\\x50\\x51\\x52\\x53\\x54\\x55\\x56\\x57\\x58\\x59\\x5a\\x5b\\x5c\\x5d\\x5e\\x5f"));
    public static final TPalette C_CYAN_DIALOG = new TPalette(TPalette.parseHexString("\\x60\\x61\\x62\\x63\\x64\\x65\\x66\\x67\\x68\\x69\\x6a\\x6b\\x6c\\x6d\\x6e\\x6f\\x70\\x71\\x72\\x73\\x74\\x75\\x76\\x77\\x78\\x79\\x7a\\x7b\\x7c\\x7d\\x7e\\x7f"));

    private DialogPalette dialogPalette = DialogPalette.DP_GRAY_DIALOG;

    public TDialog(TRect bounds, String title) {
        super(bounds, title, WN_NO_NUMBER);
        this.growMode.clear();
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

