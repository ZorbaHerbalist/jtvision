package info.qbnet.jtvision.backend;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.stage.Stage;

import java.io.InputStream;

/**
 * JavaFX-based backend that renders text using a TTF VGA-style font.
 */
public class TTFFontFxBackend implements Backend {

    private static final int CHAR_WIDTH = 9;
    private static final int CHAR_HEIGHT = 16;
    private final Screen buffer;
    private final Canvas canvas;
    private final Font font;

    public TTFFontFxBackend(Screen buffer) {
        this.buffer = buffer;
        this.canvas = new Canvas(buffer.getWidth() * CHAR_WIDTH, buffer.getHeight() * CHAR_HEIGHT);

        try (InputStream fontStream = getClass().getResourceAsStream("/PxPlus_IBM_VGA_9x16.ttf")) {
            if (fontStream == null) throw new RuntimeException("Font not found: PxPlus_IBM_VGA_9x16.ttf");
            this.font = Font.loadFont(fontStream, 16);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load TTF font", e);
        }

        drawToCanvas();
    }

    @Override
    public void render() {
        Platform.runLater(this::drawToCanvas);
    }

    private void drawToCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFont(font);
        gc.setFontSmoothingType(FontSmoothingType.LCD);

        for (int y = 0; y < buffer.getHeight(); y++) {
            for (int x = 0; x < buffer.getWidth(); x++) {
                drawChar(gc, x, y, buffer.getChar(x, y));
            }
        }
    }

    private void drawChar(GraphicsContext gc, int x, int y, Screen.ScreenChar sc) {
        double dx = x * CHAR_WIDTH;
        double dy = (y + 1) * CHAR_HEIGHT - 3; // vertical align

        gc.setFill(convert(sc.background));
        gc.fillRect(dx, dy - CHAR_HEIGHT + 3, CHAR_WIDTH, CHAR_HEIGHT);

        gc.setFill(convert(sc.foreground));
        gc.fillText(Character.toString(sc.character), dx, dy);
    }

    private Color convert(java.awt.Color awtColor) {
        return Color.rgb(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());
    }

    public Canvas getCanvas() {
        return canvas;
    }
}
