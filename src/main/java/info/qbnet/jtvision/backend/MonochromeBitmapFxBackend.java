package info.qbnet.jtvision.backend;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.InputStream;

/**
 * JavaFX-based backend that renders bitmap glyphs from a monochrome font atlas.
 */
public class MonochromeBitmapFxBackend implements Backend {

    private static final int CHAR_WIDTH = 8;
    private static final int CHAR_HEIGHT = 16;
    private final Screen buffer;
    private final Canvas canvas;
    private final Image fontAtlas;

    public MonochromeBitmapFxBackend(Screen buffer) {
        this.buffer = buffer;
        this.canvas = new Canvas(buffer.getWidth() * CHAR_WIDTH, buffer.getHeight() * CHAR_HEIGHT);

        InputStream fontStream = getClass().getResourceAsStream("/bios_font_8x16.png");
        if (fontStream == null) throw new RuntimeException("Font image not found: bios_font_8x16.png");
        this.fontAtlas = new Image(fontStream, CHAR_WIDTH * 16, CHAR_HEIGHT * 16, false, false);

        drawToCanvas();
    }

    @Override
    public void render() {
        Platform.runLater(this::drawToCanvas);
    }

    private void drawToCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        for (int y = 0; y < buffer.getHeight(); y++) {
            for (int x = 0; x < buffer.getWidth(); x++) {
                drawChar(gc, x, y, buffer.getChar(x, y));
            }
        }
    }

    private void drawChar(GraphicsContext gc, int x, int y, Screen.ScreenChar sc) {
        int charCode = sc.character & 0xFF;
        int sx = (charCode % 16) * CHAR_WIDTH;
        int sy = (charCode / 16) * CHAR_HEIGHT;

        double dx = x * CHAR_WIDTH;
        double dy = y * CHAR_HEIGHT;

        gc.setFill(convert(sc.background));
        gc.fillRect(dx, dy, CHAR_WIDTH, CHAR_HEIGHT);

        gc.drawImage(fontAtlas,
                sx, sy, CHAR_WIDTH, CHAR_HEIGHT,
                dx, dy, CHAR_WIDTH, CHAR_HEIGHT);

        gc.setGlobalBlendMode(javafx.scene.effect.BlendMode.SRC_ATOP);
        gc.setFill(convert(sc.foreground));
        gc.fillRect(dx, dy, CHAR_WIDTH, CHAR_HEIGHT);
        gc.setGlobalBlendMode(javafx.scene.effect.BlendMode.SRC_OVER);
    }

    private Color convert(java.awt.Color awtColor) {
        return Color.rgb(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());
    }

    public Canvas getCanvas() {
        return canvas;
    }
}
