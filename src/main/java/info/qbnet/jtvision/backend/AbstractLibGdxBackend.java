package info.qbnet.jtvision.backend;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import info.qbnet.jtvision.backend.factory.GuiComponent;
import info.qbnet.jtvision.backend.util.ColorUtil;
import info.qbnet.jtvision.core.Screen;

import java.util.concurrent.CountDownLatch;

/**
 * Base class for LibGDX backends handling common initialization and rendering
 * logic. Subclasses only need to provide font specific setup and glyph drawing.
 */
public abstract class AbstractLibGdxBackend extends ApplicationAdapter
        implements GuiComponent<ApplicationAdapter> {

    private final Screen screen;
    private final Integer charWidth;
    private final Integer charHeight;

    private CountDownLatch initializationLatch;

    protected SpriteBatch batch;
    protected Texture pixel;
    protected OrthographicCamera camera;
    protected ScreenViewport viewport;

    protected AbstractLibGdxBackend(Screen screen, int charWidth, int charHeight) {
        this.screen = screen;
        this.charWidth = charWidth;
        this.charHeight = charHeight;
    }

    @Override
    public void afterInitialization() {
        initResources();
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        afterInitialization();

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixel = new Texture(pixmap);
        pixmap.dispose();

        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        viewport.apply();
        camera.position.set(screen.getWidth() * charWidth / 2f, screen.getHeight() * charHeight / 2f, 0);
        camera.update();

        if (initializationLatch != null) {
            initializationLatch.countDown();
        }
    }

    @Override
    public void renderScreen() {
        if (Gdx.app != null) {
            Gdx.app.postRunnable(this::render);
        }
        else {
            System.err.println("Rendering thread not ready. Ignoring render request.");
        }
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        for (int y = 0; y < screen.getHeight(); y++) {
            for (int x = 0; x < screen.getWidth(); x++) {
                Screen.ScreenChar ch = screen.getChar(x, y);
                int pixelY = (screen.getHeight() - y - 1) * charHeight;

                batch.setColor(ColorUtil.toGdx(ch.getBackground()));
                batch.draw(pixel, x * charWidth, pixelY, charWidth, charHeight);

                drawGlyph(batch, ch, x, pixelY);
            }
        }

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        pixel.dispose();
        disposeResources();
    }

    @Override
    public Integer getCellWidth() {
        return charWidth;
    }

    @Override
    public Integer getCellHeight() {
        return charHeight;
    }

    /**
     * Initialize font or texture resources required for rendering characters.
     */
    protected abstract void initResources();

    /**
     * Draw a single glyph at the specified cell.
     *
     * @param batch  sprite batch used for drawing
     * @param ch     screen character
     * @param x      cell x coordinate
     * @param pixelY  pixel y coordinate flipped for LibGDX
     */
    protected abstract void drawGlyph(SpriteBatch batch, Screen.ScreenChar ch, int x, int pixelY);

    /**
     * Dispose any resources allocated in {@link #initResources()}.
     */
    protected abstract void disposeResources();

    @Override
    public ApplicationAdapter getNativeComponent() {
        return this;
    }

    public void setInitializationLatch(CountDownLatch latch) {
        this.initializationLatch = latch;
    }
}

