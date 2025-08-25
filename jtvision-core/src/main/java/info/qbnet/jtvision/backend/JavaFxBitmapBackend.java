package info.qbnet.jtvision.backend;

import info.qbnet.jtvision.util.Screen;
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
    protected void initializeResources() {
        log.info("Loading font atlas...");
        try (InputStream fontStream = getClass().getResourceAsStream("/font_white_8x16_2.png")) {
            if (fontStream == null) {
                log.error("Font image not found: font_white_8x16.png");
                throw new RuntimeException("Font image not found: font_white_8x16.png");
            }
            this.fontAtlas = new Image(fontStream, getCellWidth() * 16, getCellHeight() * 16, false, false);
        } catch (IOException e) {
            log.error("Failed to load font atlas", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void drawGlyph(GraphicsContext gc, int x, int y, char ch,
                              java.awt.Color fgColor, java.awt.Color bgColor) {
        int charCode = ch & 0xFF;
        int sourceX = (charCode % 16) * getCellWidth();
        int sourceY = (charCode / 16) * getCellHeight();
        double destX = x * getCellWidth();
        double destY = y * getCellHeight();

        // Draw background
        gc.setFill(ColorUtil.toFx(bgColor));
        gc.fillRect(destX, destY, getCellWidth(), getCellHeight());

        // Extract glyph from atlas
        PixelReader reader = fontAtlas.getPixelReader();
        WritableImage glyph = new WritableImage(getCellWidth(), getCellHeight());
        PixelWriter writer = glyph.getPixelWriter();
        Color fg = ColorUtil.toFx(fgColor);

        for (int j = 0; j < getCellHeight(); j++) {
            for (int i = 0; i < getCellWidth(); i++) {
                Color color = reader.getColor(sourceX + i, sourceY + j);
                // assume a white pixel means glyph, preserve alpha
                if (color.getOpacity() > 0.1) {
                    writer.setColor(i, j, fg);
                } else {
                    writer.setColor(i, j, Color.TRANSPARENT);
                }
            }
        }

        gc.drawImage(glyph, destX, destY);
    }
}
