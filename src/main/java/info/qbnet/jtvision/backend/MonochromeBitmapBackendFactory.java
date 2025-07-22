package info.qbnet.jtvision.backend;

import javax.swing.*;
import java.io.IOException;

/**
 * Factory for MonochromeBitmapBackend.
 */
public class MonochromeBitmapBackendFactory implements BackendFactory {

    private JFrame frame;

    @Override
    public void initialize() {
        frame = new JFrame("DOS Console (Monochrome Bitmap Font)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public Backend createBackend(Screen buffer) {
        try {
            MonochromeBitmapBackend panel = new MonochromeBitmapBackend(buffer);
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            return panel;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create monochrome bitmap backend", e);
        }
    }
}
