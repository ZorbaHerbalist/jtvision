package info.qbnet.jtvdemo;

import info.qbnet.jtvision.util.Command;
import info.qbnet.jtvision.util.JsonViewStore;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.views.TButton;
import info.qbnet.jtvision.views.TDialog;
import info.qbnet.jtvision.views.TInputLine;
import info.qbnet.jtvision.views.TStaticText;
import info.qbnet.jtvision.views.TLabel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Utility application that writes a simple dialog into a JSON file.
 */
public final class DialogJsonWriter {

    private DialogJsonWriter() {
    }

    public static void main(String[] args) throws IOException {
        TDialog dialog = new TDialog(new TRect(0, 0, 40, 9), "Sample dialog");
        TStaticText label = new TStaticText(new TRect(2, 1, 18, 2), "Enter text:");
        TInputLine input = new TInputLine(new TRect(2, 3, 30, 4), 100);
        TLabel buddy = new TLabel(new TRect(2, 2, 18, 3), "~T~ext:", input);
        TButton ok = new TButton(new TRect(14, 5, 26, 7), "~O~K", Command.CM_OK, TButton.BF_DEFAULT);

        dialog.insert(label);
        dialog.insert(input);
        dialog.insert(buddy);
        dialog.insert(ok);
        dialog.selectNext(false);

        String outputPath = args.length > 0 ? args[0] : "sampleDialog.json";
        try (OutputStream out = new FileOutputStream(outputPath)) {
            JsonViewStore.store(out, dialog);
        }
    }
}

