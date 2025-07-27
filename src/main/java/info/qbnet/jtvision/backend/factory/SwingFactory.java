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
    protected GuiComponent<JPanel> initializeBackend(Screen screen,
                                                    CountDownLatch latch,
                                                    Thread mainThread) {
        log.info("Starting Swing backend");

        // the backend is created inside the EDT, store it in an array so we
        // can return it after initialization completes
        final GuiComponent<JPanel>[] ref = new GuiComponent[1];

        // perform all UI initialization on the Swing event dispatch thread
        SwingUtilities.invokeLater(() -> {
            // create the backend inside the EDT to avoid threading issues
            GuiComponent<JPanel> backend = constructor.apply(screen);

            JFrame frame = new JFrame();
            frame.setTitle(createWindowTitle(backend));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(backend.getNativeComponent());
            frame.pack();
            frame.setVisible(true);
            log.debug("Swing frame shown");

            backend.afterInitialization();

            setupThreadCleanup(mainThread, () -> {
                log.debug("Forcefully terminating Swing frame...");
                SwingUtilities.invokeLater(frame::dispose);
            });

            // store backend reference for the caller and signal completion
            ref[0] = backend;
            latch.countDown();
        });

        // wait for the Swing thread to finish setting up the UI
        awaitInitialization(latch);
        // return the backend created on the EDT
        return ref[0];
    }
}
