package info.qbnet.jtvision.backend.factory;

import info.qbnet.jtvision.backend.LanternaBackend;
import info.qbnet.jtvision.backend.factory.GuiComponent;
import info.qbnet.jtvision.util.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

/**
 * Factory for creating Lanterna based backends.
 */
public class LanternaFactory extends Factory<GuiComponent<com.googlecode.lanterna.screen.Screen>> {

    private static final Logger log = LoggerFactory.getLogger(LanternaFactory.class);

    public LanternaFactory(Function<Screen, ? extends GuiComponent<com.googlecode.lanterna.screen.Screen>> constructor) {
        super(constructor, "Lanterna");
    }

    @Override
    protected GuiComponent<com.googlecode.lanterna.screen.Screen> initializeBackend(Screen screen, CountDownLatch latch, Thread mainThread) {
        log.info("Starting Lanterna backend");
        GuiComponent<com.googlecode.lanterna.screen.Screen> backend = constructor.apply(screen);
        backend.afterInitialization();

        if (backend instanceof LanternaBackend lb) {
            setupThreadCleanup(mainThread, lb::stop);
        }

        latch.countDown();
        return backend;
    }
}
