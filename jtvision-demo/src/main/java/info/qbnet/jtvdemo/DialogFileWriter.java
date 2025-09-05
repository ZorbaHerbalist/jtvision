package info.qbnet.jtvdemo;

import info.qbnet.jtvision.util.Command;
import info.qbnet.jtvision.views.TButton;
import info.qbnet.jtvision.views.TDialog;
import info.qbnet.jtvision.views.TInputLine;
import info.qbnet.jtvision.views.TStaticText;
import info.qbnet.jtvision.util.TRect;
import info.qbnet.jtvision.util.TStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Utility class that creates a simple dialog and stores it in a binary file.
 */
public final class DialogFileWriter {

    private DialogFileWriter() {
    }

    public static void main(String[] args) throws IOException {
        // Create dialog with some controls
        TDialog dialog = new TDialog(new TRect(0, 0, 40, 9), "Sample dialog");
        TStaticText label = new TStaticText(new TRect(2, 1, 18, 2), "Enter text:");
        TInputLine input = new TInputLine(new TRect(2, 3, 30, 4), 100);
        TButton ok = new TButton(new TRect(14, 5, 26, 7), "~O~K", Command.CM_OK, TButton.BF_DEFAULT);

        dialog.insert(label);
        dialog.insert(input);
        dialog.insert(ok);
        dialog.selectNext(false);

        String outputPath = args.length > 0 ? args[0] : "sampleDialog.bin";
        try (OutputStream out = new FileOutputStream(outputPath)) {
            TStream stream = new TStream(out);
            stream.storeView(dialog);
        }
    }
}
