package info.qbnet.jtvision.backend;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import java.util.function.Function;

/**
 * Generic LibGDX backend factory accepting ApplicationAdapter constructor.
 */
public class LibGdxBackendFactory implements BackendFactory {

    private final Function<Screen, ApplicationAdapter> constructor;

    public LibGdxBackendFactory(Function<Screen, ApplicationAdapter> constructor) {
        this.constructor = constructor;
    }

    @Override
    public void initialize() {
        // No-op for LibGDX
    }

    @Override
    public Backend createBackend(Screen buffer) {
        ApplicationAdapter app = constructor.apply(buffer);

        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "DOS Console (LibGDX)";
        config.width = buffer.getWidth() * 11; // updated to match CHAR_WIDTH
        config.height = buffer.getHeight() * 20; // updated to match CHAR_HEIGHT

        new LwjglApplication(app, config);
        return new DummyBackend();
    }

    private static class DummyBackend implements Backend {
        @Override
        public void render() {}
    }
}
