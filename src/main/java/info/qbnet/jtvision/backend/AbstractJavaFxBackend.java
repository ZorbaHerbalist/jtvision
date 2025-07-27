package info.qbnet.jtvision.backend;

import info.qbnet.jtvision.backend.factory.GuiComponent;
import info.qbnet.jtvision.core.Screen;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

/**
 * Base class for JavaFX backends implementing common rendering logic.
 * Subclasses only need to provide font setup and glyph drawing.
 */
public abstract class AbstractJavaFxBackend implements GuiComponent<Canvas> {

    protected final Screen screen;
    protected final Canvas canvas;
    private final Integer charWidth;
    private final Integer charHeight;

    protected AbstractJavaFxBackend(Screen screen, int charWidth, int charHeight) {
        this.screen = screen;
        this.charWidth = charWidth;
        this.charHeight = charHeight;
        this.canvas = new Canvas(screen.getWidth() * charWidth, screen.getHeight() * charHeight);
    }

    @Override
    public void renderScreen() {
        Platform.runLater(this::drawToCanvas);
    }

    protected void drawToCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        configureGraphics(gc);
        for (int y = 0; y < screen.getHeight(); y++) {
            for (int x = 0; x < screen.getWidth(); x++) {
                drawGlyph(gc, x, y, screen.getChar(x, y));
            }
        }
    }

    /**
     * Initializes any JavaFX resources required by the backend. This method
     * is expected to be called on the JavaFX Application Thread.
     */
    @Override
    public final void afterInitialization() {
        initResources();
        drawToCanvas();
    }

    /**
     * Hook for subclasses to load fonts or images before rendering.
     */
    protected abstract void initResources();

    /**
     * Hook for subclasses to configure the graphics context before drawing.
     * Default implementation does nothing.
     */
    protected void configureGraphics(GraphicsContext gc) {
        // no-op
    }

    protected abstract void drawGlyph(GraphicsContext gc, int x, int y, Screen.ScreenChar sc);

    @Override
    public Integer getCharWidth() {
        return charWidth;
    }

    @Override
    public Integer getCharHeight() {
        return charHeight;
    }

    @Override
    public Canvas getNativeComponent() {
        return canvas;
    }
}
