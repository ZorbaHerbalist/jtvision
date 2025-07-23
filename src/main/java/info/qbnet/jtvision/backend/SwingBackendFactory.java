package info.qbnet.jtvision.backend;

import javax.swing.*;
import java.util.function.Function;

public class SwingBackendFactory implements BackendFactory {

    private final Function<Screen, ? extends SwingBackendWithPanel> constructor;

    public SwingBackendFactory(Function<Screen, ? extends SwingBackendWithPanel> constructor) {
        this.constructor = constructor;
    }

    @Override
    public void initialize() {
        // no-op for Swing
    }

    @Override
    public Backend createBackend(Screen buffer) {
        try {
            SwingBackendWithPanel backend = constructor.apply(buffer);
            JFrame frame = new JFrame("Console (Library: Swing, Renderer: " + backend.getClass().getSimpleName() + ")");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(backend.getPanel());
            frame.pack();
            frame.setVisible(true);
            return backend;
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to create Swing backend", e);
        }
    }

    public interface SwingBackendWithPanel extends Backend {
        JPanel getPanel();
    }
}