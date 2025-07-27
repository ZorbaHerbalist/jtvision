package info.qbnet.jtvision.backend.factory;

import info.qbnet.jtvision.core.Screen;

import javax.swing.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwingFactory extends Factory<GuiComponent<JPanel>> {

    private static final Logger log = LoggerFactory.getLogger(SwingFactory.class);

    public SwingFactory(Function<Screen, ? extends GuiComponent<JPanel>> constructor) {
        super(constructor, "Swing");
        log.debug("SwingFactory created");
    }

    @Override
    protected void initializeBackend(GuiComponent<JPanel> backend,
                                    int pixelWidth, int pixelHeight,
                                    CountDownLatch latch, Thread mainThread) {
        log.info("Starting Swing backend");
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setTitle(createWindowTitle(backend));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(backend.getNativeComponent());
            frame.pack();
            frame.setVisible(true);
            log.debug("Swing frame shown");

            backend.initialize();

            setupThreadCleanup(mainThread, () -> {
                log.debug("Forcefully terminating Swing frame...");
                SwingUtilities.invokeLater(frame::dispose);
            });

            latch.countDown();
        });
    }
}
