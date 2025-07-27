package info.qbnet.jtvision.backend;

import info.qbnet.jtvision.backend.factory.GuiComponent;
import info.qbnet.jtvision.util.Screen;
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
    private final Integer cellWidth;
    private final Integer cellHeight;

    protected AbstractJavaFxBackend(Screen screen, int cellWidth, int cellHeight) {
        this.screen = screen;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.canvas = new Canvas(screen.getWidth() * cellWidth, screen.getHeight() * cellHeight);
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

    protected abstract void drawGlyph(GraphicsContext gc, int x, int y, Screen.CharacterCell sc);

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
}
