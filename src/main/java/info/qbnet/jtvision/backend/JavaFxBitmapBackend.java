package info.qbnet.jtvision.backend;

import info.qbnet.jtvision.backend.factory.JavaFxFactory;
import info.qbnet.jtvision.core.Screen;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import info.qbnet.jtvision.backend.util.ColorUtil;

import java.io.InputStream;

/**
 * JavaFX backend rendering bitmap glyphs with pre-colored white font atlas.
 */
public class JavaFxBitmapBackend implements JavaFxFactory.FxBackendWithCanvas {

    private static final int CHAR_WIDTH = 8;
    private static final int CHAR_HEIGHT = 16;
    private final Screen buffer;
    private final Canvas canvas;
    private final Image fontAtlas;

    public JavaFxBitmapBackend(Screen buffer) {
        this.buffer = buffer;
        this.canvas = new Canvas(buffer.getWidth() * CHAR_WIDTH, buffer.getHeight() * CHAR_HEIGHT);

        InputStream fontStream = getClass().getResourceAsStream("/font_white_8x16_2.png");
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
        int charCode = sc.getCharacter() & 0xFF;
        int sx = (charCode % 16) * CHAR_WIDTH;
        int sy = (charCode / 16) * CHAR_HEIGHT;
        double dx = x * CHAR_WIDTH;
        double dy = y * CHAR_HEIGHT;

        // Draw background
        gc.setFill(ColorUtil.toFx(sc.getBackground()));
        gc.fillRect(dx, dy, CHAR_WIDTH, CHAR_HEIGHT);

        // Extract glyph from atlas
        PixelReader reader = fontAtlas.getPixelReader();
        WritableImage glyph = new WritableImage(CHAR_WIDTH, CHAR_HEIGHT);
        PixelWriter writer = glyph.getPixelWriter();
        Color fg = ColorUtil.toFx(sc.getForeground());

        for (int j = 0; j < CHAR_HEIGHT; j++) {
            for (int i = 0; i < CHAR_WIDTH; i++) {
                Color color = reader.getColor(sx + i, sy + j);
                // assume a white pixel means glyph, preserve alpha
                if (color.getOpacity() > 0.1) {
                    writer.setColor(i, j, fg);
                } else {
                    writer.setColor(i, j, Color.TRANSPARENT);
                }
            }
        }

        gc.drawImage(glyph, dx, dy);
    }

    @Override
    public Canvas getCanvas() {
        return canvas;
    }
}