package info.qbnet.jtvision.util;

import info.qbnet.jtvision.views.TProgram;
import info.qbnet.jtvision.views.TButton;
import info.qbnet.jtvision.views.TDialog;
import info.qbnet.jtvision.views.TStaticText;
import info.qbnet.jtvision.views.TView;

import java.util.ArrayList;
import java.util.List;

public class MsgBox {

    private MsgBox() {
        // utility class
    }

    /** Message box classes. */
    public static final int MF_WARNING      = 0x0000;
    public static final int MF_ERROR        = 0x0001;
    public static final int MF_INFORMATION  = 0x0002;
    public static final int MF_CONFIRMATION = 0x0003;

    /** Insert message box into application instead of desktop. */
    public static final int MF_INSERT_IN_APP = 0x0080;

    /** Message box button flags. */
    public static final int MF_YES_BUTTON    = 0x0100;
    public static final int MF_NO_BUTTON     = 0x0200;
    public static final int MF_OK_BUTTON     = 0x0400;
    public static final int MF_CANCEL_BUTTON = 0x0800;

    /** Convenience combinations. */
    public static final int MF_YES_NO_CANCEL = MF_YES_BUTTON | MF_NO_BUTTON | MF_CANCEL_BUTTON;
    public static final int MF_OK_CANCEL     = MF_OK_BUTTON | MF_CANCEL_BUTTON;

    private static final String[] BUTTON_NAMES = {"~Y~es", "~N~o", "O~K~", "Cancel"};
    private static final int[] BUTTON_COMMANDS = {Command.CM_YES, Command.CM_NO, Command.CM_OK, Command.CM_CANCEL};
    private static final String[] TITLES = {"Warning", "Error", "Information", "Confirm"};

    /**
     * Displays a standard sized message box centred on the desktop or
     * application.
     *
     * @param msg     the message (may contain format specifiers understood by
     *                {@link String#format})
     * @param options combination of {@code MF_*} constants
     * @param params  optional parameters for {@link String#format}
     * @return command of the pressed button
     */
    public static int messageBox(String msg, int options, Object... params) {
        TRect r = new TRect(0, 0, 40, 9);
        TRect area = new TRect();
        if ((options & MF_INSERT_IN_APP) == 0) {
            if (TProgram.desktop != null) {
                TProgram.desktop.getExtent(area);
            }
        } else {
            if (TProgram.application != null) {
                TProgram.application.getExtent(area);
            }
        }
        r.move((area.b.x - r.b.x) / 2, (area.b.y - r.b.y) / 2);
        return messageBoxRect(r, msg, options, params);
    }

    /**
     * Displays a message box using the supplied bounds.
     */
    public static int messageBoxRect(TRect r, String msg, int options, Object... params) {
        String title = TITLES[options & 0x3];
        TDialog dialog = new TDialog(r, title);

        // text
        TRect tr = new TRect();
        tr.assign(3, 2, dialog.getSize().x - 2, dialog.getSize().y - 3);
        String text = params != null && params.length > 0 ? String.format(msg, params) : msg;
        dialog.insert(new TStaticText(tr, text));

        // create buttons
        List<TView> buttons = new ArrayList<>();
        int x = -2;
        for (int i = 0; i < 4; i++) {
            int mask = 0x0100 << i;
            if ((options & mask) != 0) {
                TRect br = new TRect(0, 0, 10, 2);
                TButton b = new TButton(br, BUTTON_NAMES[i], BUTTON_COMMANDS[i], TButton.BF_NORMAL);
                x += b.getSize().x + 2;
                buttons.add(b);
            }
        }
        x = (dialog.getSize().x - x) / 2;
        for (TView v : buttons) {
            dialog.insert(v);
            v.moveTo(x, dialog.getSize().y - 3);
            x += v.getSize().x + 2;
        }

        int res;
        if ((options & MF_INSERT_IN_APP) == 0) {
            res = (TProgram.desktop != null) ? TProgram.desktop.execView(dialog) : Command.CM_CANCEL;
        } else {
            res = (TProgram.application != null) ? TProgram.application.execView(dialog) : Command.CM_CANCEL;
        }
        return res;
    }

}
