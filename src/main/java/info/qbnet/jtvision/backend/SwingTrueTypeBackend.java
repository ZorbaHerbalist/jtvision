package info.qbnet.jtvision.backend;

import info.qbnet.jtvision.core.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.io.IOException;
import java.io.InputStream;

/**
 * A backend that renders screen characters using a TTF VGA font with pixel-perfect rendering.
 */
public class SwingTrueTypeBackend extends AbstractSwingBackend {

    private static final Logger log = LoggerFactory.getLogger(SwingTrueTypeBackend.class);

    private Font font;

    public SwingTrueTypeBackend(Screen screen, int charWidth, int charHeight) {
        super(screen, charWidth, charHeight);
        afterInitialization();
    }

    @Override
    public void afterInitialization() {
        initResources();
    }

    /** Load the TTF font resource. */
    protected void initResources() {
        log.info("Loading TrueType font...");
        try (InputStream fontStream = getClass().getResourceAsStream("/PxPlus_IBM_VGA_9x16.ttf")) {
            if (fontStream == null) {
                log.error("Font TTF IBM_VGA_9x16.ttf not found in resources.");
                throw new RuntimeException("Font TTF IBM_VGA_9x16.ttf not found in resources.");
            }
            this.font = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.PLAIN, 16f);
        } catch (FontFormatException | IOException e) {
            log.error("Failed to load font TTF IBM_VGA_9x16.ttf", e);
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backBuffer, 0, 0, null);
    }

    @Override
    protected void drawGlyph(Graphics2D g, int x, int y, Screen.ScreenChar sc) {
        g.setFont(font);
        int pixelX = x * getCellWidth();
        int pixelY = y * getCellHeight();

        g.setColor(sc.getBackground());
        g.fillRect(pixelX, pixelY, getCellWidth(), getCellHeight());

        g.setColor(sc.getForeground());
        FontRenderContext fontRenderContext = new FontRenderContext(null, false, false);
        GlyphVector glyphVector = font.createGlyphVector(fontRenderContext, new char[] { sc.getCharacter() });
        g.drawGlyphVector(glyphVector, pixelX, pixelY + getCellHeight() - 3);
    }

}
