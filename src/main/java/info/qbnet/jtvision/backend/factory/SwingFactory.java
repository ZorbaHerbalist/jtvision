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
    public SwingBackendWithPanel createBackend(Screen buffer) {
        Thread mainThread = Thread.currentThread();

        return createAndInitialize(buffer, (backend, latch) ->
                SwingUtilities.invokeLater(() -> {
                    JFrame frame = new JFrame();
                    frame.setTitle("Console (Library: Swing, Renderer: " +
                            backend.getClass().getSimpleName() + ")");
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setContentPane(backend.getPanel());
                    frame.pack();
                    frame.setVisible(true);

                    Runnable dispose = () -> SwingUtilities.invokeLater(frame::dispose);
                    ThreadWatcher.onTermination(mainThread, dispose);
                    Runtime.getRuntime().addShutdownHook(new Thread(dispose));

                    latch.countDown();
                }));
    }

    public interface SwingBackendWithPanel extends Backend {
        JPanel getPanel();
    }
}
