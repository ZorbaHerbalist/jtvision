package info.qbnet.jtvision.backend.factory;

import info.qbnet.jtvision.backend.Backend;
import info.qbnet.jtvision.core.Screen;

import javax.swing.*;
import info.qbnet.jtvision.backend.util.ThreadWatcher;
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
            JFrame frame = new JFrame();
            // Create and show the UI on the Event Dispatch Thread
            SwingUtilities.invokeAndWait(() -> {
                frame.setTitle("Console (Library: Swing, Renderer: " +
                        backend.getClass().getSimpleName() + ")");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setContentPane(backend.getPanel());
                frame.pack();
                frame.setVisible(true);
            });

            // Close window when the calling thread terminates
            ThreadWatcher.onTermination(Thread.currentThread(),
                    () -> SwingUtilities.invokeLater(frame::dispose));

            // Also add shutdown hook for JVM shutdown scenarios
            Runtime.getRuntime().addShutdownHook(new Thread(() ->
                    SwingUtilities.invokeLater(frame::dispose)));

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
