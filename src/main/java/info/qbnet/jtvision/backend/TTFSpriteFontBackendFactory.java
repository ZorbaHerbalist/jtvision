package info.qbnet.jtvision.backend;

import javax.swing.*;
import java.io.IOException;
import java.awt.FontFormatException;

/**
 * Factory for creating a TTFSpriteFontBackend.
 */
public class TTFSpriteFontBackendFactory implements BackendFactory {

    private JFrame frame;

    @Override
    public void initialize() {
        frame = new JFrame("DOS Console (TTF Sprite Font)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public Backend createBackend(Screen buffer) {
        try {
            TTFSpriteFontBackend panel = new TTFSpriteFontBackend(buffer);
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            return panel;
        } catch (IOException | FontFormatException e) {
            throw new RuntimeException("Failed to create TTF sprite font backend", e);
        }
    }
}