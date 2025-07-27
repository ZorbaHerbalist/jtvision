package info.qbnet.jtvision.backend.factory;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import info.qbnet.jtvision.backend.AbstractLibGdxBackend;
import info.qbnet.jtvision.core.Screen;

import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic LibGDX backend factory using constructor injection.
 */
public class LibGdxFactory extends Factory<GuiComponent<ApplicationAdapter>> {

    private static final Logger log = LoggerFactory.getLogger(LibGdxFactory.class);

    public LibGdxFactory(Function<Screen, ? extends GuiComponent<ApplicationAdapter>> constructor) {
        super(constructor, "LibGDX");
        log.debug("LibGdxFactory created");
    }

    @Override
    protected GuiComponent<ApplicationAdapter> initializeBackend(Screen screen,
                                                                 CountDownLatch latch,
                                                                 Thread mainThread) {
        GuiComponent<ApplicationAdapter> backend = constructor.apply(screen);

        log.info("Starting LibGDX backend");

        if (backend instanceof AbstractLibGdxBackend initBackend) {
            initBackend.setInitializationLatch(latch);
        }

        int pixelWidth = screen.getWidth() * backend.getCharWidth();
        int pixelHeight = screen.getHeight() * backend.getCharHeight();

        LwjglApplicationConfiguration lwjglConfig = new LwjglApplicationConfiguration();
        lwjglConfig.title = createWindowTitle(backend);
        lwjglConfig.width = pixelWidth;
        lwjglConfig.height = pixelHeight;

        ApplicationAdapter adapter = backend.getNativeComponent();
        Thread uiThread = new Thread(() -> {
            log.debug("Starting LibGDX UI thread...");
            new LwjglApplication(adapter, lwjglConfig);
            log.debug("LibGDX UI thread completed");
        });
        uiThread.setName("uiThread");
        uiThread.setDaemon(true);
        uiThread.start();
        log.debug("LibGDX UI thread initialized");

        setupThreadCleanup(mainThread, () -> {
            log.debug("Forcefully terminating LibGDX UI thread...");
            if (Gdx.app != null) {
                Gdx.app.postRunnable(() -> Gdx.app.exit());
            }
        });

        return backend;
    }
}
