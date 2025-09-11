package info.qbnet.cubecmd;

import info.qbnet.jtvision.backend.factory.BackendType;
import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.KeyCode;
import info.qbnet.jtvision.util.MsgBox;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.views.TApplication;
import info.qbnet.jtvision.views.TMenuBar;

public class CubeCmdApp extends TApplication {

    public static final int CM_MANAGER_NEW = 101;

    public CubeCmdApp() {
        super(BackendType.JAVAFX_BITMAP);
    }

    @Override
    public void handleEvent(TEvent event) {
        super.handleEvent(event);
        if (event.what == TEvent.EV_COMMAND) {
            switch (event.msg.command) {
                case CM_MANAGER_NEW:
                    onManagerNew();
                    break;
                default:
                    return;
            }
            clearEvent(event);
        }
    }

    @Override
    public void initMenuBar() {
        TRect r = new TRect();
        getExtent(r);
        r.b.y = r.a.y + 1;
        menuBar = new TMenuBar(r, TMenuBar.menu()
                .submenu("~M~anager", HelpContext.HC_NO_CONTEXT, m -> m
                        .item("~N~ew", "Ctrl-F3", KeyCode.KB_CTRL_F3, CM_MANAGER_NEW, HelpContext.HC_NO_CONTEXT))
                .build());
    }

    private void onManagerNew() {
        MsgBox.messageBox("New manager", MsgBox.MF_INFORMATION + MsgBox.MF_OK_BUTTON);
    }

    public static void main(String[] args) {
        CubeCmdApp app = new CubeCmdApp();
        app.run();
    }
}
