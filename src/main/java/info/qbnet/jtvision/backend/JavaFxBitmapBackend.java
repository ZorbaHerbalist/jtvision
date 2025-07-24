package info.qbnet.jtvision.backend;

import info.qbnet.jtvision.core.Screen;
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
public class JavaFxBitmapBackend extends AbstractJavaFxBackend {

    private static final int CHAR_WIDTH = 8;
    private static final int CHAR_HEIGHT = 16;
    private final Image fontAtlas;

    public JavaFxBitmapBackend(Screen buffer) {
        super(buffer, CHAR_WIDTH, CHAR_HEIGHT);

        InputStream fontStream = getClass().getResourceAsStream("/font_white_8x16_2.png");
        if (fontStream == null) throw new RuntimeException("Font image not found: font_white_8x16.png");
        this.fontAtlas = new Image(fontStream, CHAR_WIDTH * 16, CHAR_HEIGHT * 16, false, false);

        drawToCanvas();
    }

    // drawToCanvas() inherited

    @Override
    protected void drawChar(GraphicsContext gc, int x, int y, Screen.ScreenChar sc) {
        int charCode = sc.getCharacter() & 0xFF;
        int sx = (charCode % 16) * getCharWidth();
        int sy = (charCode / 16) * getCharHeight();
        double dx = x * getCharWidth();
        double dy = y * getCharHeight();

        // Draw background
        gc.setFill(ColorUtil.toFx(sc.getBackground()));
        gc.fillRect(dx, dy, getCharWidth(), getCharHeight());

        // Extract glyph from atlas
        PixelReader reader = fontAtlas.getPixelReader();
        WritableImage glyph = new WritableImage(getCharWidth(), getCharHeight());
        PixelWriter writer = glyph.getPixelWriter();
        Color fg = ColorUtil.toFx(sc.getForeground());

        for (int j = 0; j < getCharHeight(); j++) {
            for (int i = 0; i < getCharWidth(); i++) {
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
}
