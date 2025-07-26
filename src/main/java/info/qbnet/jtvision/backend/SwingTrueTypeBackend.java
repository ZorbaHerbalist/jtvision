package info.qbnet.jtvision.backend;

import info.qbnet.jtvision.core.Screen;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.io.IOException;
import java.io.InputStream;

/**
 * A backend that renders screen characters using a TTF VGA font with pixel-perfect rendering.
 */
public class SwingTrueTypeBackend extends AbstractSwingBackend {

    private final Font font;

    public SwingTrueTypeBackend(Screen buffer, int charWidth, int charHeight) {
        super(buffer, charWidth, charHeight);
        InputStream fontStream = getClass().getResourceAsStream("/PxPlus_IBM_VGA_9x16.ttf");
        if (fontStream == null) {
            throw new RuntimeException("Font TTF IBM_VGA_9x16.ttf not found in resources.");
        }
        try {
            this.font = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.PLAIN, 16f);
        } catch (FontFormatException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backBuffer, 0, 0, null);
    }

    @Override
    protected void drawChar(Graphics2D g, int x, int y, Screen.ScreenChar sc) {
        g.setFont(font);
        int px = x * getCharWidth();
        int py = y * getCharHeight();

        g.setColor(sc.getBackground());
        g.fillRect(px, py, getCharWidth(), getCharHeight());

        g.setColor(sc.getForeground());
        FontRenderContext frc = new FontRenderContext(null, false, false);
        GlyphVector gv = font.createGlyphVector(frc, new char[] { sc.getCharacter() });
        g.drawGlyphVector(gv, px, py + getCharHeight() - 3);
    }

}
