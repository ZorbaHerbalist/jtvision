package info.qbnet.jtvision.backend;

import info.qbnet.jtvision.core.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * A backend that uses a 1-bit monochrome bitmap font atlas and applies color dynamically.
 */
public class SwingBitmapBackend extends AbstractSwingBackend {

    private static final Logger log = LoggerFactory.getLogger(SwingBitmapBackend.class);

    private BufferedImage fontAtlas;

    public SwingBitmapBackend(Screen screen, int charWidth, int charHeight) {
        super(screen, charWidth, charHeight);
        afterInitialization();
    }

    @Override
    public void afterInitialization() {
        initResources();
    }

    /** Load font atlas and other resources. */
    protected void initResources() {
        log.info("Loading font atlas...");
        try (InputStream fontStream = getClass().getResourceAsStream("/font_white_8x16_2.png")) {
            if (fontStream == null) {
                log.error("Missing resource: font_white_8x16_2.png");
                throw new RuntimeException("Missing resource: font_white_8x16_2.png");
            }
            this.fontAtlas = ImageIO.read(fontStream);
        } catch (IOException e) {
            log.error("Failed to load font atlas", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backBuffer, 0, 0, null);
    }

    @Override
    protected void drawGlyph(Graphics2D g, int x, int y, Screen.CharacterCell sc) {
        int charCode = sc.getCharacter() & 0xFF;
        int sourceX = (charCode % 16) * getCellWidth();
        int sourceY = (charCode / 16) * getCellHeight();

        int destX = x * getCellWidth();
        int destY = y * getCellHeight();

        // Draw background
        g.setColor(sc.getBackground());
        g.fillRect(destX, destY, getCellWidth(), getCellHeight());

        // Apply foreground color to 1-bit glyph
        for (int glyphY = 0; glyphY < getCellHeight(); glyphY++) {
            for (int glyphX = 0; glyphX < getCellWidth(); glyphX++) {
                int pixelColor = fontAtlas.getRGB(sourceX + glyphX, sourceY + glyphY) & 0xFFFFFF;
                if (pixelColor != 0x000000) {
                    g.setColor(sc.getForeground());
                    g.fillRect(destX + glyphX, destY + glyphY, 1, 1);
                }
            }
        }
    }
}