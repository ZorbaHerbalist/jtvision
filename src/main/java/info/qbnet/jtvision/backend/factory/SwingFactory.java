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

            Thread callingThread = Thread.currentThread();
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

            // Monitor main thread and close window when it terminates
            Thread mainThreadWatcher = new Thread(() -> {
                try {
                    callingThread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                // Close window when main thread terminates
                JFrame frame = frameHolder[0];
                if (frame != null) {
                    SwingUtilities.invokeLater(frame::dispose);
                }
            });
            mainThreadWatcher.setDaemon(true);
            mainThreadWatcher.start();

            // Also add shutdown hook for JVM shutdown scenarios
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