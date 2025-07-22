package info.qbnet.jtvision.backend;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * A backend that renders screen characters using a TTF VGA font with pixel-perfect rendering.
 */
public class TTFFontBackend extends JPanel implements Backend {

    private static final int CHAR_WIDTH = 9;
    private static final int CHAR_HEIGHT = 16;
    private final Screen buffer;
    private final Font font;
    private final BufferedImage backbuffer;

    public TTFFontBackend(Screen buffer) throws IOException, FontFormatException {
        this.buffer = buffer;
        InputStream fontStream = getClass().getResourceAsStream("/PxPlus_IBM_VGA_9x16.ttf");
        if (fontStream == null) {
            throw new IOException("Font TTF IBM_VGA_9x16.ttf not found in resources.");
        }
        this.font = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.PLAIN, 16f);

        int width = buffer.getWidth() * CHAR_WIDTH;
        int height = buffer.getHeight() * CHAR_HEIGHT;
        this.backbuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        setPreferredSize(new Dimension(width, height));
    }

    @Override
    public void render() {
        drawToBackbuffer();
        repaint();
    }

    private void drawToBackbuffer() {
        Graphics2D g2d = backbuffer.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g2d.setFont(font);

        for (int y = 0; y < buffer.getHeight(); y++) {
            for (int x = 0; x < buffer.getWidth(); x++) {
                drawChar(g2d, x, y, buffer.getChar(x, y));
            }
        }
        g2d.dispose();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backbuffer, 0, 0, null);
    }

    private void drawChar(Graphics2D g, int x, int y, Screen.ScreenChar sc) {
        int px = x * CHAR_WIDTH;
        int py = y * CHAR_HEIGHT;

        g.setColor(sc.background);
        g.fillRect(px, py, CHAR_WIDTH, CHAR_HEIGHT);

        g.setColor(sc.foreground);
        FontRenderContext frc = g.getFontRenderContext();
        GlyphVector gv = font.createGlyphVector(frc, new char[] { sc.character });
        g.drawGlyphVector(gv, px, py + CHAR_HEIGHT - 3);
    }
}
