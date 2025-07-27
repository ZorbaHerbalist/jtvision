package info.qbnet.jtvision.backend;

import info.qbnet.jtvision.core.Screen;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import info.qbnet.jtvision.backend.util.ColorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * JavaFX backend rendering bitmap glyphs with pre-colored white font atlas.
 */
public class JavaFxBitmapBackend extends AbstractJavaFxBackend {

    private static final Logger log = LoggerFactory.getLogger(JavaFxBitmapBackend.class);

    private Image fontAtlas;

    public JavaFxBitmapBackend(Screen screen, int charWidth, int charHeight) {
        super(screen, charWidth, charHeight);

        // initialization deferred until JavaFX stage is ready
    }

    // drawToCanvas() inherited

    @Override
    protected void initResources() {
        log.info("Loading font atlas...");
        try (InputStream fontStream = getClass().getResourceAsStream("/font_white_8x16_2.png")) {
            if (fontStream == null) {
                log.error("Font image not found: font_white_8x16.png");
                throw new RuntimeException("Font image not found: font_white_8x16.png");
            }
            this.fontAtlas = new Image(fontStream, getCharWidth() * 16, getCharHeight() * 16, false, false);
        } catch (IOException e) {
            log.error("Failed to load font atlas", e);
            throw new RuntimeException(e);
        }
    }

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
