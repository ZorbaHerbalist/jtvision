package info.qbnet.jtvision.backend;

import info.qbnet.jtvision.backend.factory.GuiComponent;
import info.qbnet.jtvision.util.Screen;
import info.qbnet.jtvision.util.DosPalette;
import info.qbnet.jtvision.backend.util.ColorUtil;
import info.qbnet.jtvision.event.KeyCodeMapper;
import info.qbnet.jtvision.event.TEvent;
import info.qbnet.jtvision.util.TPoint;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Base class for JavaFX backends implementing common rendering logic.
 * Subclasses only need to provide font setup and glyph drawing.
 */
public abstract class AbstractJavaFxBackend implements GuiComponent<Canvas> {

    protected final Screen screen;
    protected final Canvas canvas;
    private final Integer cellWidth;
    private final Integer cellHeight;
    private final Queue<TEvent> events = new ConcurrentLinkedQueue<>();
    private volatile int mouseButtons = 0;
    private volatile int mouseX = 0;
    private volatile int mouseY = 0;
    private volatile byte shiftState = 0;
    private volatile int cursorX = 0;
    private volatile int cursorY = 0;
    private volatile boolean cursorVisible = false;
    private volatile boolean cursorInsert = false;
    private volatile boolean cursorOn = true;
    private final ScheduledExecutorService cursorBlink =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "cursor-blink");
                t.setDaemon(true);
                return t;
            });
    private static final long BLINK_MS = 530;

    protected AbstractJavaFxBackend(Screen screen, int cellWidth, int cellHeight) {
        this.screen = screen;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.canvas = new Canvas(screen.getWidth() * cellWidth, screen.getHeight() * cellHeight);
        this.canvas.setFocusTraversable(true);
        this.canvas.setOnKeyPressed(e -> {
            updateShiftState(e, true);
            int code = mapKeyCode(e.getCode().getCode());
            boolean shift = e.isShiftDown();
            boolean ctrl = e.isControlDown();
            boolean alt = e.isAltDown();
            int withMods = KeyCodeMapper.applyModifiers(code, shift, ctrl, alt);
            char ch = KeyCodeMapper.toChar(code, shift);
            int scan = code;
            TEvent ev = new TEvent();
            ev.what = TEvent.EV_KEYDOWN;
            ev.key.keyCode = withMods;
            ev.key.charCode = ch;
            ev.key.scanCode = (byte) scan;
            events.add(ev);
        });
        this.canvas.setOnKeyReleased(e -> updateShiftState(e, false));

        this.canvas.setOnMousePressed(e -> {
            updateMousePosition(e.getX(), e.getY());
            switch (e.getButton()) {
                case PRIMARY -> mouseButtons |= 1;
                case SECONDARY -> mouseButtons |= 2;
            }
        });

        this.canvas.setOnMouseReleased(e -> {
            updateMousePosition(e.getX(), e.getY());
            switch (e.getButton()) {
                case PRIMARY -> mouseButtons &= ~1;
                case SECONDARY -> mouseButtons &= ~2;
            }
        });

        this.canvas.setOnMouseMoved(e -> updateMousePosition(e.getX(), e.getY()));
        this.canvas.setOnMouseDragged(e -> updateMousePosition(e.getX(), e.getY()));

        cursorBlink.scheduleAtFixedRate(() ->
                Platform.runLater(() -> {
                    cursorOn = !cursorOn;
                    renderToCanvas();
                }), BLINK_MS, BLINK_MS, TimeUnit.MILLISECONDS);
    }

    private void updateMousePosition(double px, double py) {
        int x = (int) (px / cellWidth);
        int y = (int) (py / cellHeight);
        x = Math.max(0, Math.min(screen.getWidth() - 1, x));
        y = Math.max(0, Math.min(screen.getHeight() - 1, y));
        mouseX = x;
        mouseY = y;
    }

    @Override
    public void renderScreen() {
        Platform.runLater(this::renderToCanvas);
    }

    protected void renderToCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        configureGraphics(gc);
        for (int y = 0; y < screen.getHeight(); y++) {
            for (int x = 0; x < screen.getWidth(); x++) {
                short cell = screen.getCell(x, y);
                char ch = (char) (cell & 0xFF);
                int attr = (cell >>> 8) & 0xFF;
                java.awt.Color fg = DosPalette.getForeground(attr);
                java.awt.Color bg = DosPalette.getBackground(attr);
                drawGlyph(gc, x, y, ch, fg, bg);
            }
        }
        if (cursorVisible && cursorOn) {
            short cell = screen.getCell(cursorX, cursorY);
            int attr = (cell >>> 8) & 0xFF;
            java.awt.Color fg = DosPalette.getForeground(attr);
            gc.setFill(ColorUtil.toFx(fg));
            double px = cursorX * cellWidth;
            double py = cursorY * cellHeight;
            if (cursorInsert) {
                gc.fillRect(px, py, cellWidth, cellHeight);
            } else {
                double h = Math.max(1, cellHeight / 8.0);
                gc.fillRect(px, py + cellHeight - h, cellWidth, h);
            }
        }
    }

    /**
     * Initializes any JavaFX resources required by the backend. This method
     * is expected to be called on the JavaFX Application Thread.
     */
    @Override
    public final void afterInitialization() {
        initializeResources();
        renderToCanvas();
    }

    /**
     * Hook for subclasses to load fonts or images before rendering.
     */
    protected abstract void initializeResources();

    /**
     * Hook for subclasses to configure the graphics context before drawing.
     * Default implementation does nothing.
     */
    protected void configureGraphics(GraphicsContext gc) {
        // no-op
    }

    protected abstract void drawGlyph(GraphicsContext gc, int x, int y, char ch,
                                      java.awt.Color fg, java.awt.Color bg);

    @Override
    public Integer getCellWidth() {
        return cellWidth;
    }

    @Override
    public Integer getCellHeight() {
        return cellHeight;
    }

    @Override
    public Canvas getUIComponent() {
        return canvas;
    }

    private void updateShiftState(KeyEvent e, boolean pressed) {
        KeyCode code = e.getCode();
        switch (code) {
            case SHIFT -> {
                if (pressed) {
                    shiftState |= 0x03; // JavaFX doesn't distinguish left/right
                } else {
                    shiftState &= ~0x03;
                }
            }
            case CONTROL -> {
                if (pressed) {
                    shiftState |= 0x04;
                } else {
                    shiftState &= ~0x04;
                }
            }
            case ALT, ALT_GRAPH -> {
                if (pressed) {
                    shiftState |= 0x08;
                } else {
                    shiftState &= ~0x08;
                }
            }
            default -> {
                // ignore other keys
            }
        }
    }

    @Override
    public byte getShiftState() {
        return shiftState;
    }

    /**
     * Maps the JavaFX key code to the unified scheme. JavaFX uses the same
     * numeric values as AWT so the value is returned unchanged.
     */
    public static int mapKeyCode(int keyCode) {
        return keyCode;
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

    @Override
    public void updateCursor(int x, int y, boolean insertMode, boolean visible) {
        cursorX = x;
        cursorY = y;
        cursorInsert = insertMode;
        cursorVisible = visible;
        cursorOn = true;
        Platform.runLater(this::renderToCanvas);
    }
}
