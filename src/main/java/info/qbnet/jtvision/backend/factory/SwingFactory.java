package info.qbnet.jtvision.backend.factory;

import info.qbnet.jtvision.backend.Backend;
import info.qbnet.jtvision.core.Screen;

import javax.swing.*;
import java.awt.Frame;
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

            Thread uiThread = new Thread(() -> {
                JFrame frame = new JFrame(
                        "Console (Library: Swing, Renderer: " + backend.getClass().getSimpleName() + ")");
                frameHolder[0] = frame;
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setContentPane(backend.getPanel());
                frame.pack();
                frame.setVisible(true);
            }, "Swing UI Thread");
            uiThread.setDaemon(true);
            uiThread.start();

            Thread watcher = new Thread(() -> {
                try {
                    callingThread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                JFrame frame = frameHolder[0];
                if (frame != null) {
                    SwingUtilities.invokeLater(frame::dispose);
                }
            }, "Swing UI Watcher");
            watcher.setDaemon(true);
            watcher.start();

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