package info.qbnet.jtvision.backend;

import javax.swing.*;
import java.io.IOException;
import java.awt.FontFormatException;

/**
 * Factory for creating a TTFFontBackend.
 */
public class TTFFontBackendFactory implements BackendFactory {

    private JFrame frame;

    @Override
    public void initialize() {
        frame = new JFrame("DOS Console (TTF Font)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public Backend createBackend(Screen buffer) {
        try {
            TTFFontBackend panel = new TTFFontBackend(buffer);
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            return panel;
        } catch (IOException | FontFormatException e) {
            throw new RuntimeException("Failed to create TTF font backend", e);
        }
    }
}
