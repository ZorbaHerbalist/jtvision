package info.qbnet.jtvision.backend.factory;

import info.qbnet.jtvision.backend.Backend;
import info.qbnet.jtvision.core.Screen;

import javax.swing.*;
import java.util.function.Function;

public class SwingFactory implements Factory {

    private final Function<Screen, ? extends SwingBackendWithPanel> constructor;

    public SwingFactory(Function<Screen, ? extends SwingBackendWithPanel> constructor) {
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

            final JFrame[] frameHolder = new JFrame[1];

            // Create and show the UI on the Event Dispatch Thread
            SwingUtilities.invokeAndWait(() -> {
                JFrame frame = new JFrame(
                        "Console (Library: Swing, Renderer: " + backend.getClass().getSimpleName() + ")");
                frameHolder[0] = frame;
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setContentPane(backend.getPanel());
                frame.pack();
                frame.setVisible(true);
            });

            // Add shutdown hook to close window when main thread terminates
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                JFrame frame = frameHolder[0];
                if (frame != null) {
                    SwingUtilities.invokeLater(frame::dispose);
                }
            }));

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