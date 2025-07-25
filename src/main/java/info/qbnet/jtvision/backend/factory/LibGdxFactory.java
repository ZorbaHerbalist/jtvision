package info.qbnet.jtvision.backend.factory;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import info.qbnet.jtvision.backend.Backend;
import info.qbnet.jtvision.core.Screen;

import java.util.function.Function;

/**
 * Generic LibGDX backend factory accepting ApplicationAdapter constructor.
 */
public class LibGdxFactory implements Factory {

    private final Function<Screen, ApplicationAdapter> constructor;

    public LibGdxFactory(Function<Screen, ApplicationAdapter> constructor) {
        this.constructor = constructor;
    }

    @Override
    public void initialize() {
        // No-op for LibGDX
    }

    @Override
    public Backend createBackend(Screen buffer) {
        ApplicationAdapter app = constructor.apply(buffer);

        // Ensure the app implements Backend interface
        if (!(app instanceof Backend)) {
            throw new IllegalArgumentException("LibGDX ApplicationAdapter must implement Backend interface");
        }

        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "DOS Console (LibGDX)";
        config.width = buffer.getWidth() * 8;
        config.height = buffer.getHeight() * 16;

        // Start LibGDX application in a separate thread
        Thread libgdxThread = new Thread(() -> {
            new LwjglApplication(app, config);
        });
        libgdxThread.setDaemon(true);
        libgdxThread.start();

        // Wait a moment for the application to initialize
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Return the actual backend instance
        return (Backend) app;
    }
}
