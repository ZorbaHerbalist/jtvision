package info.qbnet.jtvision.backend;

import javax.swing.*;

/**
 * Factory for initializing and creating a Swing rendering backend.
 */
public class SwingBackendFactory implements BackendFactory {

    private JFrame frame;

    @Override
    public Backend createBackend(Screen buffer) {
        SwingBackend panel = new SwingBackend(buffer);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        return panel;
    }

    @Override
    public void initialize() {
        frame = new JFrame("DOS Console");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
