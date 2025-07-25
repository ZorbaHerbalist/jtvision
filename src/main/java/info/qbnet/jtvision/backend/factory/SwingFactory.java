package info.qbnet.jtvision.backend.factory;

import info.qbnet.jtvision.backend.Backend;
import info.qbnet.jtvision.core.Screen;

import javax.swing.*;
import info.qbnet.jtvision.backend.util.ThreadWatcher;
import java.util.function.Function;
import java.util.concurrent.CountDownLatch;

public class SwingFactory extends AbstractGuiFactory<SwingFactory.SwingBackendWithPanel> {

    public SwingFactory(Function<Screen, ? extends SwingBackendWithPanel> constructor) {
        super(constructor);
    }

    @Override
    public Backend createBackend(Screen buffer) {
        CountDownLatch latch = new CountDownLatch(1);

        Thread mainThread = Thread.currentThread();

        SwingBackendWithPanel backend = createBackendInstance(buffer);

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

        awaitInitialization(latch);

        return backend;
    }

    public interface SwingBackendWithPanel extends Backend {
        JPanel getPanel();
    }
}
