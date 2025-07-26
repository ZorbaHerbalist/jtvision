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

    protected final Screen buffer;
    protected final Canvas canvas;
    private final Integer charWidth;
    private final Integer charHeight;

    protected AbstractJavaFxBackend(Screen buffer, int charWidth, int charHeight) {
        this.buffer = buffer;
        this.charWidth = charWidth;
        this.charHeight = charHeight;
        this.canvas = new Canvas(buffer.getWidth() * charWidth, buffer.getHeight() * charHeight);
    }

    @Override
    public void render() {
        Platform.runLater(this::drawToCanvas);
    }

    protected void drawToCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        configureGraphics(gc);
        for (int y = 0; y < buffer.getHeight(); y++) {
            for (int x = 0; x < buffer.getWidth(); x++) {
                drawChar(gc, x, y, buffer.getChar(x, y));
            }
        }
    }

    /**
     * Hook for subclasses to configure the graphics context before drawing.
     * Default implementation does nothing.
     */
    protected void configureGraphics(GraphicsContext gc) {
        // no-op
    }

    protected abstract void drawChar(GraphicsContext gc, int x, int y, Screen.ScreenChar sc);

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
