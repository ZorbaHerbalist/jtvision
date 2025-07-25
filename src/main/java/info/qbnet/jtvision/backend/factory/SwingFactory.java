package info.qbnet.jtvision.backend.factory;

import info.qbnet.jtvision.backend.Backend;
import info.qbnet.jtvision.core.Screen;

import javax.swing.*;
import info.qbnet.jtvision.backend.util.ThreadWatcher;
import java.util.function.Function;
import java.util.concurrent.CountDownLatch;

public class SwingFactory implements Factory {

    private final Function<Screen, ? extends SwingBackendWithPanel> constructor;

    public SwingFactory(Function<Screen, ? extends SwingBackendWithPanel> constructor) {
        this.constructor = constructor;
    }

    @Override
    public Backend createBackend(Screen buffer) {
        SwingBackendWithPanel backend = constructor.apply(buffer);

        CountDownLatch latch = new CountDownLatch(1);
        Thread mainThread = Thread.currentThread();

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setTitle("Console (Library: Swing, Renderer: " +
                    backend.getClass().getSimpleName() + ")");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(backend.getPanel());
            frame.pack();
            frame.setVisible(true);

            Runnable dispose = () -> SwingUtilities.invokeLater(frame::dispose);
            // Close window when the calling thread terminates
            ThreadWatcher.onTermination(mainThread, dispose);
            // Also add shutdown hook for JVM shutdown scenarios
            Runtime.getRuntime().addShutdownHook(new Thread(dispose));

            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        return backend;
    }

    public interface SwingBackendWithPanel extends Backend {
        JPanel getPanel();
    }
}
