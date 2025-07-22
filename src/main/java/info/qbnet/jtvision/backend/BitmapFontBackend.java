package info.qbnet.jtvision.backend;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * A backend that renders screen characters using a bitmap font from a PNG file.
 * Pixel-perfect rendering is enforced by disabling image smoothing and scaling artifacts.
 * Rendering is done into a BufferedImage backbuffer for precise control.
 */
public class BitmapFontBackend extends JPanel implements Backend {

    private static final int CHAR_WIDTH = 9;
    private static final int CHAR_HEIGHT = 16;
    private final Screen buffer;
    private final BufferedImage fontImage;
    private final BufferedImage backbuffer;

    public BitmapFontBackend(Screen buffer) throws IOException {
        this.buffer = buffer;
        InputStream fontStream = getClass().getResourceAsStream("/cp437_9x16.png");
        if (fontStream == null) {
            throw new IOException("Font image cp437_9x16.png not found in resources.");
        }
        this.fontImage = ImageIO.read(fontStream);

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
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
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

    private void drawChar(Graphics g, int x, int y, Screen.ScreenChar sc) {
        int code = sc.character & 0xFF;
        int sx = (code % 16) * CHAR_WIDTH;
        int sy = (code / 16) * CHAR_HEIGHT;
        BufferedImage glyph = fontImage.getSubimage(sx, sy, CHAR_WIDTH, CHAR_HEIGHT);

        int px = x * CHAR_WIDTH;
        int py = y * CHAR_HEIGHT;

        g.setColor(sc.background);
        g.fillRect(px, py, CHAR_WIDTH, CHAR_HEIGHT);

        for (int gy = 0; gy < CHAR_HEIGHT; gy++) {
            for (int gx = 0; gx < CHAR_WIDTH; gx++) {
                int pixel = glyph.getRGB(gx, gy) & 0xFFFFFF;
                if (pixel != 0x000000) {
                    g.setColor(sc.foreground);
                    g.fillRect(px + gx, py + gy, 1, 1);
                }
            }
        }
    }
}