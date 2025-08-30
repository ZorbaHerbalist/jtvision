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
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import info.qbnet.jtvision.backend.factory.GuiComponent;
import info.qbnet.jtvision.backend.util.ColorUtil;
import info.qbnet.jtvision.util.Screen;
import info.qbnet.jtvision.util.DosPalette;
import info.qbnet.jtvision.core.event.KeyCodeMapper;
import info.qbnet.jtvision.core.event.TEvent;
import info.qbnet.jtvision.core.objects.TPoint;

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
    private final Queue<TEvent> events = new ConcurrentLinkedQueue<>();
    private volatile int mouseButtons = 0;
    private volatile int mouseX = 0;
    private volatile int mouseY = 0;
    private volatile byte shiftState = 0;
    private int cursorX = 0;
    private int cursorY = 0;
    private boolean cursorVisible = false;
    private boolean cursorInsert = false;
    private boolean cursorOn = true;
    private long lastBlink = 0;
    private static final long BLINK_MS = 530;

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
                updateShiftState();
                int code = mapKeyCode(keycode);
                boolean shift = (shiftState & 0x03) != 0;
                boolean ctrl = (shiftState & 0x04) != 0;
                boolean alt = (shiftState & 0x08) != 0;
                int withMods = KeyCodeMapper.applyModifiers(code, shift, ctrl, alt);
                char ch = KeyCodeMapper.toChar(code, shift);
                int scan = code;
                TEvent ev = new TEvent();
                ev.what = TEvent.EV_KEYDOWN;
                ev.key.keyCode = withMods;
                ev.key.charCode = ch;
                ev.key.scanCode = (byte) scan;
                events.add(ev);
                return true;
            }

            @Override
            public boolean keyUp(int keycode) {
                updateShiftState();
                return true;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                updateMousePosition(screenX, screenY);
                if (button == Input.Buttons.LEFT) {
                    mouseButtons |= 1;
                } else if (button == Input.Buttons.RIGHT) {
                    mouseButtons |= 2;
                }
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                updateMousePosition(screenX, screenY);
                if (button == Input.Buttons.LEFT) {
                    mouseButtons &= ~1;
                } else if (button == Input.Buttons.RIGHT) {
                    mouseButtons &= ~2;
                }
                return true;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                updateMousePosition(screenX, screenY);
                return true;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                updateMousePosition(screenX, screenY);
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
        long now = TimeUtils.millis();
        if (now - lastBlink >= BLINK_MS) {
            cursorOn = !cursorOn;
            lastBlink = now;
        }
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

        if (cursorVisible && cursorOn) {
            short cell = screen.getCell(cursorX, cursorY);
            int attr = (cell >>> 8) & 0xFF;
            java.awt.Color fg = DosPalette.getForeground(attr);
            batch.setColor(ColorUtil.toGdx(fg));
            int pixelY = (screen.getHeight() - cursorY - 1) * cellHeight;
            if (cursorInsert) {
                batch.draw(pixel, cursorX * cellWidth, pixelY, cellWidth, cellHeight);
            } else {
                int h = Math.max(1, cellHeight / 8);
                batch.draw(pixel, cursorX * cellWidth, pixelY, cellWidth, h);
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
        return Optional.ofNullable(events.poll());
    }

    @Override
    public int getMouseButtons() {
        return mouseButtons;
    }

    @Override
    public TPoint getMouseLocation() {
        return new TPoint(mouseX, mouseY);
    }

    private void updateShiftState() {
        byte state = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) state |= 0x01;
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) state |= 0x02;
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) state |= 0x04;
        if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT)) state |= 0x08;
        shiftState = state;
    }

    @Override
    public byte getShiftState() {
        updateShiftState();
        return shiftState;
    }

    private void updateMousePosition(int screenX, int screenY) {
        int x = screenX / cellWidth;
        int y = screenY / cellHeight;
        x = Math.max(0, Math.min(screen.getWidth() - 1, x));
        y = Math.max(0, Math.min(screen.getHeight() - 1, y));
        mouseX = x;
        mouseY = y;
    }

    @Override
    public void updateCursor(int x, int y, boolean insertMode, boolean visible) {
        cursorX = x;
        cursorY = y;
        cursorInsert = insertMode;
        cursorVisible = visible;
        cursorOn = true;
    }
}

