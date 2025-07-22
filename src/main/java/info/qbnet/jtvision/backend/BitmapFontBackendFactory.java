package info.qbnet.jtvision.backend;

import javax.swing.*;
import java.io.IOException;

/**
 * Factory for creating a BitmapFontBackend.
 */
public class BitmapFontBackendFactory implements BackendFactory {

    private JFrame frame;

    @Override
    public void initialize() {
        frame = new JFrame("DOS Console (Bitmap Font)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public Backend createBackend(Screen buffer) {
        try {
            BitmapFontBackend panel = new BitmapFontBackend(buffer);
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            return panel;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create bitmap font backend", e);
        }
    }
}
