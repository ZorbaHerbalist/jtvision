package info.qbnet.cubecmd;

import info.qbnet.jtvision.backend.factory.BackendType;
import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.KeyCode;
import info.qbnet.jtvision.util.MsgBox;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.views.TApplication;
import info.qbnet.jtvision.views.TMenuBar;
import info.qbnet.jtvision.views.TMenuPopup;
import info.qbnet.jtvision.views.TView.HelpContext;
import info.qbnet.jtvision.views.TWindow;

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
        TRect r = new TRect(0, 0, 20, 6);
        desktop.getExtent(r);
        r.a.x = (r.b.x - 20) / 2;
        r.a.y = (r.b.y - 6) / 2;
        r.b.x = r.a.x + 20;
        r.b.y = r.a.y + 6;

        TMenuPopup popup = new TMenuPopup(r, TMenuBar.menu()
                .item("C:", null, KeyCode.KB_NO_KEY, 2001, HelpContext.HC_NO_CONTEXT)
                .item("H:", null, KeyCode.KB_NO_KEY, 2002, HelpContext.HC_NO_CONTEXT)
                .item("I:", null, KeyCode.KB_NO_KEY, 2003, HelpContext.HC_NO_CONTEXT)
                .build());
        int res = desktop.execView(popup);
        popup.done();

        switch (res) {
            case 2001:
                //MsgBox.messageBox("First option", MsgBox.MF_INFORMATION + MsgBox.MF_OK_BUTTON);
                break;
            case 2002:
                MsgBox.messageBox("Unknown drive H:", MsgBox.MF_INFORMATION + MsgBox.MF_OK_BUTTON);
                break;
            case 2003:
                MsgBox.messageBox("Unknown drive I:", MsgBox.MF_INFORMATION + MsgBox.MF_OK_BUTTON);
                break;
            default:
                break;
        }

        desktop.getExtent(r);
        TDoubleWindow window = new TDoubleWindow(r, TWindow.WN_NO_NUMBER);
        insertWindow(window);
    }

    public static void main(String[] args) {
        CubeCmdApp app = new CubeCmdApp();
        app.run();
    }
}
