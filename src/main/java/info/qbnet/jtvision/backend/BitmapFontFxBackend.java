package info.qbnet.jtvision.backend;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.effect.BlendMode;
import javafx.stage.Stage;

import java.io.InputStream;

/**
 * JavaFX backend rendering bitmap glyphs with pre-colored white font atlas.
 */
public class BitmapFontFxBackend implements Backend {

    private static final int CHAR_WIDTH = 8;
    private static final int CHAR_HEIGHT = 16;
    private final Screen buffer;
    private final Canvas canvas;
    private final Image fontAtlas;

    public BitmapFontFxBackend(Screen buffer) {
        this.buffer = buffer;
        this.canvas = new Canvas(buffer.getWidth() * CHAR_WIDTH, buffer.getHeight() * CHAR_HEIGHT);

        InputStream fontStream = getClass().getResourceAsStream("/font_white_8x16.png");
        if (fontStream == null) throw new RuntimeException("Font image not found: font_white_8x16.png");
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

        // Draw background
        gc.setFill(convert(sc.background));
        gc.fillRect(dx, dy, CHAR_WIDTH, CHAR_HEIGHT);

        // Extract glyph from atlas
        javafx.scene.image.PixelReader reader = fontAtlas.getPixelReader();
        javafx.scene.image.WritableImage glyph = new javafx.scene.image.WritableImage(CHAR_WIDTH, CHAR_HEIGHT);
        javafx.scene.image.PixelWriter writer = glyph.getPixelWriter();
        javafx.scene.paint.Color fg = convert(sc.foreground);

        for (int j = 0; j < CHAR_HEIGHT; j++) {
            for (int i = 0; i < CHAR_WIDTH; i++) {
                Color color = reader.getColor(sx + i, sy + j);
                // assume white pixel means glyph, preserve alpha
                if (color.getOpacity() > 0.1) {
                    writer.setColor(i, j, fg);
                } else {
                    writer.setColor(i, j, Color.TRANSPARENT);
                }
            }
        }

        gc.drawImage(glyph, dx, dy);
    }


    private Color convert(java.awt.Color awtColor) {
        return Color.rgb(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());
    }

    public Canvas getCanvas() {
        return canvas;
    }
}