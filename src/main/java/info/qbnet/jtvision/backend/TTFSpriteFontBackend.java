package info.qbnet.jtvision.backend;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * A backend that renders characters using a pre-rendered TTF font atlas to ensure pixel-perfect DOS-like appearance.
 */
public class TTFSpriteFontBackend extends JPanel implements SwingBackendFactory.SwingBackendWithPanel {

    private static final int CHAR_WIDTH = 9;
    private static final int CHAR_HEIGHT = 16;
    private final Screen buffer;
    private final BufferedImage[] glyphs = new BufferedImage[256];
    private final BufferedImage backbuffer;

    public TTFSpriteFontBackend(Screen buffer) {
        this.buffer = buffer;

        // Load font and generate glyph bitmaps
        InputStream fontStream = getClass().getResourceAsStream("/PxPlus_IBM_VGA_9x16.ttf");
        if (fontStream == null) {
            throw new RuntimeException("Font TTF PxPlus_IBM_VGA_9x16.ttf not found in resources.");
        }
        Font font = null;
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.PLAIN, 16f);
        } catch (FontFormatException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        FontRenderContext frc = new FontRenderContext(null, false, false);

        for (int i = 0; i < 256; i++) {
            BufferedImage img = new BufferedImage(CHAR_WIDTH, CHAR_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            g.setFont(font);
            g.setColor(Color.WHITE);
            GlyphVector gv = font.createGlyphVector(frc, new char[] { (char) i });
            g.drawGlyphVector(gv, 0, CHAR_HEIGHT - 3);
            g.dispose();
            glyphs[i] = img;
        }

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
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

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

        BufferedImage glyph = glyphs[sc.character & 0xFF];
        for (int gy = 0; gy < CHAR_HEIGHT; gy++) {
            for (int gx = 0; gx < CHAR_WIDTH; gx++) {
                int pixel = glyph.getRGB(gx, gy);
                if ((pixel >> 24) != 0x00) { // non-transparent
                    g.setColor(sc.foreground);
                    g.fillRect(px + gx, py + gy, 1, 1);
                }
            }
        }
    }

    @Override
    public JPanel getPanel() {
        return this;
    }
}