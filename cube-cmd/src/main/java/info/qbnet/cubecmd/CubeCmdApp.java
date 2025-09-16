package info.qbnet.cubecmd;

import info.qbnet.jtvision.backend.factory.BackendType;
import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.*;
import info.qbnet.jtvision.views.*;
import info.qbnet.jtvision.views.TView.HelpContext;

import java.io.File;
import java.util.Arrays;

public class CubeCmdApp extends TApplication {

    public static final int CM_PANEL_SETUP = 101;
    public static final int CM_MANAGER_NEW = 111;

    public CubeCmdApp() {
        super(BackendType.JAVAFX_BITMAP);
    }

    @Override
    public void handleEvent(TEvent event) {
        super.handleEvent(event);
        if (event.what == TEvent.EV_COMMAND) {
            switch (event.msg.command) {
                case CM_PANEL_SETUP:
                    onPanelSetup();
                    break;
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
                .submenu("~P~anel", HelpContext.HC_NO_CONTEXT, m -> m
                        .item("~S~etup panel", "Alt-S", KeyCode.KB_ALT_S, CM_PANEL_SETUP, HelpContext.HC_NO_CONTEXT))
                .submenu("~M~anager", HelpContext.HC_NO_CONTEXT, m -> m
                        .item("~N~ew", "Ctrl-F3", KeyCode.KB_CTRL_F3, CM_MANAGER_NEW, HelpContext.HC_NO_CONTEXT))
                .build());
    }

    private void onPanelSetup() {
        TDialog d = new TDialog(new TRect(0, 0, 49, 18), "Panel options");
        d.options |= Options.OF_CENTER;

        TRadioButtons radio = new TRadioButtons(new TRect(2, 3, 47, 5),
                Arrays.asList("~N~ame" ,"~E~xtension", "~S~ize", "~T~ime", "~G~roup", "~U~nsorted"));
        d.insert(radio);
        d.insert(new TLabel(new TRect(2, 2, 47, 3), "Sort by:", radio));

        TCheckBoxes check = new TCheckBoxes(new TRect(2, 7, 47, 11),
                Arrays.asList("Directories first", "Executable first", "Archives first"));
        d.insert(check);
        d.insert(new TLabel(new TRect(2, 6, 47, 7), "Display:", check));

        TInputLine input = new TInputLine(new TRect(2, 13, 44, 14), 250);
        d.insert(input);
        d.insert(new TLabel(new TRect(2, 12, 44, 13), "File ~m~ask:", input));

        d.insert(new TButton(new TRect(9, 15, 19, 17), "OK", Command.CM_OK, TButton.BF_DEFAULT));
        d.insert(new TButton(new TRect(19, 15, 29, 17), "Cancel", Command.CM_CANCEL, 0));

        d.selectNext(false);

        int sortBy = 0x0002;
        int displayFlags = 0x0003;
        DataPacket data = new DataPacket(d.dataSize() * 2)
                .putShort((short) sortBy)
                .putShort((short) displayFlags)
                .putStringField("*.*", input.dataSize())
                .rewind();
        d.setData(data.getByteBuffer());

        desktop.execView(d);
    }

    private File selectDrive(int x, int y, File defaultDrive, boolean includeTemp) {

        File[] roots = File.listRoots();
        TMenuBar.MenuBuilder builder = TMenuBar.menu();
        int maxLength = 0;
        for (int i = 0; i < roots.length; i++) {
            String name = roots[i].getAbsolutePath();
            if (maxLength < name.length())
                maxLength = name.length();
            builder.item("  ~" + name + "~  ", null, KeyCode.KB_NO_KEY, 2001 + i, HelpContext.HC_NO_CONTEXT);
        }

        TRect r = new TRect();
        desktop.getExtent(r);

        r.a.x = x;
        r.a.y = y;
        if (x < 0) {
            r.a.x = 0;
        } else if (x + maxLength + 4 > r.b.x) {
            r.a.x = r.b.x - maxLength - 4;
        }
        if (y < 0) {
            r.a.y = 0;
        } else if (y + roots.length + 2 > r.b.y) {
            r.a.y = r.b.y - roots.length - 2;
        }
        r.b.x = r.a.x + maxLength + 4;
        r.b.y = r.a.y + roots.length + 2;

        TMenuPopup popup = new TMenuPopup(r, builder.build());
        int res = desktop.execView(popup);
        popup.done();
        File selected = null;
        if (res >= 2001 && res < 2001 + roots.length) {
            selected = roots[res - 2001];
            MsgBox.messageBox("Selected drive " + selected.getAbsolutePath(),
                    MsgBox.MF_INFORMATION + MsgBox.MF_OK_BUTTON);
        }

        return selected;
    }

    private void openWindow(boolean selectDrive) {
        File drive;
        if (selectDrive) {
            TRect r = new TRect();
            desktop.getExtent(r);
            drive = selectDrive(r.a.x + (r.b.x - r.a.x) / 2, r.a.y, null, false);
        } else {
            drive = File.listRoots()[0];
        }

        TRect r = new TRect();
        desktop.getExtent(r);
        TDoubleWindow window = new TDoubleWindow(r, TWindow.WN_NO_NUMBER, drive);
        insertWindow(window);
    }

    private void onManagerNew() {
        openWindow(true);
    }

    public static void main(String[] args) {
        CubeCmdApp app = new CubeCmdApp();
        app.run();
    }
}
