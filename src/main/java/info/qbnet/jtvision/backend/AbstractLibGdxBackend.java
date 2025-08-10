package info.qbnet.jtvision.backend;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import info.qbnet.jtvision.backend.factory.GuiComponent;
import info.qbnet.jtvision.backend.util.ColorUtil;
import info.qbnet.jtvision.util.Screen;
import info.qbnet.jtvision.util.DosPalette;
import info.qbnet.jtvision.core.event.KeyCodeMapper;
import info.qbnet.jtvision.core.event.TEvent;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

/**
 * Base class for LibGDX backends handling common initialization and rendering
 * logic. Subclasses only need to provide font specific setup and glyph drawing.
 */
public abstract class AbstractLibGdxBackend extends ApplicationAdapter
        implements GuiComponent<ApplicationAdapter> {

    private final Screen screen;
    private final Integer cellWidth;
    private final Integer cellHeight;

    private CountDownLatch initializationLatch;
    private final Queue<TEvent> keyEvents = new ConcurrentLinkedQueue<>();

    protected SpriteBatch batch;
    protected Texture pixel;
    protected OrthographicCamera camera;
    protected ScreenViewport viewport;

    protected AbstractLibGdxBackend(Screen screen, int cellWidth, int cellHeight) {
        this.screen = screen;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
    }

    @Override
    public void afterInitialization() {
        initializeResources();
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
        camera.position.set(screen.getWidth() * cellWidth / 2f, screen.getHeight() * cellHeight / 2f, 0);
        camera.update();

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                int code = mapKeyCode(keycode);
                boolean shift = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ||
                        Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
                boolean ctrl = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) ||
                        Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
                boolean alt = Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) ||
                        Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT);
                int withMods = KeyCodeMapper.applyModifiers(code, shift, ctrl, alt);
                char ch = KeyCodeMapper.toChar(code, shift);
                int scan = code;
                TEvent ev = new TEvent();
                ev.what = TEvent.EV_KEYDOWN;
                ev.key.keyCode = withMods;
                ev.key.charCode = ch;
                ev.key.scanCode = (byte) scan;
                keyEvents.add(ev);
                return true;
            }
        });

        if (initializationLatch != null) {
            initializationLatch.countDown();
        }
    }

    @Override
    public void renderScreen() {
        // no-op for LibGDX
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        for (int y = 0; y < screen.getHeight(); y++) {
            for (int x = 0; x < screen.getWidth(); x++) {
                short cell = screen.getCell(x, y);
                char ch = (char) (cell & 0xFF);
                int attr = (cell >>> 8) & 0xFF;
                java.awt.Color fg = DosPalette.getForeground(attr);
                java.awt.Color bg = DosPalette.getBackground(attr);
                int pixelY = (screen.getHeight() - y - 1) * cellHeight;

                batch.setColor(ColorUtil.toGdx(bg));
                batch.draw(pixel, x * cellWidth, pixelY, cellWidth, cellHeight);

                drawGlyph(batch, ch, fg, x, pixelY);
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
        return cellWidth;
    }

    @Override
    public Integer getCellHeight() {
        return cellHeight;
    }

    /**
     * Initialize font or texture resources required for rendering characters.
     */
    protected abstract void initializeResources();

    /**
     * Draw a single glyph at the specified cell.
     *
     * @param batch  sprite batch used for drawing
     * @param ch     character to draw
     * @param fg     foreground colour
     * @param x      cell x coordinate
     * @param pixelY pixel y coordinate flipped for LibGDX
     */
    protected abstract void drawGlyph(SpriteBatch batch, char ch, java.awt.Color fg, int x, int pixelY);

    /**
     * Dispose any resources allocated in {@link #initializeResources()}.
     */
    protected abstract void disposeResources();

    @Override
    public ApplicationAdapter getUIComponent() {
        return this;
    }

    /**
     * Maps a LibGDX key code to the unified scheme based on AWT constants.
     */
    public static int mapKeyCode(int keyCode) {
        if (keyCode >= Input.Keys.A && keyCode <= Input.Keys.Z) {
            return java.awt.event.KeyEvent.VK_A + (keyCode - Input.Keys.A);
        }
        if (keyCode >= Input.Keys.NUM_0 && keyCode <= Input.Keys.NUM_9) {
            return java.awt.event.KeyEvent.VK_0 + (keyCode - Input.Keys.NUM_0);
        }
        switch (keyCode) {
            case Input.Keys.F1:  return java.awt.event.KeyEvent.VK_F1;
            case Input.Keys.F2:  return java.awt.event.KeyEvent.VK_F2;
            case Input.Keys.F3:  return java.awt.event.KeyEvent.VK_F3;
            case Input.Keys.F4:  return java.awt.event.KeyEvent.VK_F4;
            case Input.Keys.F5:  return java.awt.event.KeyEvent.VK_F5;
            case Input.Keys.F6:  return java.awt.event.KeyEvent.VK_F6;
            case Input.Keys.F7:  return java.awt.event.KeyEvent.VK_F7;
            case Input.Keys.F8:  return java.awt.event.KeyEvent.VK_F8;
            case Input.Keys.F9:  return java.awt.event.KeyEvent.VK_F9;
            case Input.Keys.F10: return java.awt.event.KeyEvent.VK_F10;
            case Input.Keys.F11: return java.awt.event.KeyEvent.VK_F11;
            case Input.Keys.F12: return java.awt.event.KeyEvent.VK_F12;
            case Input.Keys.LEFT:  return java.awt.event.KeyEvent.VK_LEFT;
            case Input.Keys.RIGHT: return java.awt.event.KeyEvent.VK_RIGHT;
            case Input.Keys.UP:    return java.awt.event.KeyEvent.VK_UP;
            case Input.Keys.DOWN:  return java.awt.event.KeyEvent.VK_DOWN;
            case Input.Keys.ENTER: return java.awt.event.KeyEvent.VK_ENTER;
            case Input.Keys.SPACE: return java.awt.event.KeyEvent.VK_SPACE;
            case Input.Keys.ESCAPE:return java.awt.event.KeyEvent.VK_ESCAPE;
            case Input.Keys.TAB:   return java.awt.event.KeyEvent.VK_TAB;
            case Input.Keys.BACKSPACE: return java.awt.event.KeyEvent.VK_BACK_SPACE;
            default:
                return keyCode;
        }
    }

    public void setInitializationLatch(CountDownLatch latch) {
        this.initializationLatch = latch;
    }

    @Override
    public Optional<TEvent> pollEvent() {
        return Optional.ofNullable(keyEvents.poll());
    }
}

