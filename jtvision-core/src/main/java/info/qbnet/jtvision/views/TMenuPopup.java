package info.qbnet.jtvision.views;

import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.TMenu;
import info.qbnet.jtvision.util.TMenuItem;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.util.TStream;
import info.qbnet.jtvision.util.KeyCode;

public class TMenuPopup extends TMenuBox {

    public static final int CLASS_ID = 42;

    public static void registerType() {
        TStream.registerType(CLASS_ID, TMenuPopup::new);
    }

    public TMenuPopup(TRect bounds, TMenu menu) {
        super(bounds, menu, null);
    }

    public TMenuPopup(TStream stream) {
        super(stream);
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    @Override
    public void store(TStream stream) {
        super.store(stream);
    }

    @Override
    public void handleEvent(TEvent event) {
        if (event.what == TEvent.EV_KEYDOWN) {
            TMenuItem p = findItem(KeyCode.getCtrlChar(event.key.keyCode));
            if (p == null) {
                p = hotKey(event.key.keyCode);
            }
            if (p != null && commandEnabled(p.command)) {
                event.what = TEvent.EV_COMMAND;
                event.msg.command = p.command;
                event.msg.infoPtr = null;
                putEvent(event);
                clearEvent(event);
            } else if (KeyCode.getAltChar(event.key.keyCode) != '\0') {
                clearEvent(event);
            }
        }
        super.handleEvent(event);
    }
}

