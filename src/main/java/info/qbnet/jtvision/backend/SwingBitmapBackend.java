package info.qbnet.jtvision.backend;

import info.qbnet.jtvision.backend.factory.SwingFactory;
import info.qbnet.jtvision.core.Screen;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * A backend that uses a 1-bit monochrome bitmap font atlas and applies color dynamically.
 */
public class SwingBitmapBackend extends JPanel implements SwingFactory.SwingBackendWithPanel {

    private static final int CHAR_WIDTH = 8;
    private static final int CHAR_HEIGHT = 16;
    private final Screen buffer;
    private final BufferedImage fontAtlas;
    private final BufferedImage backBuffer;

    public SwingBitmapBackend(Screen buffer) {
        this.buffer = buffer;

        InputStream stream = getClass().getResourceAsStream("/bios_font_8x16.png");
        if (stream == null) {
            throw new RuntimeException("Missing resource: bios_font_8x16.png");
        }

        try {
            this.fontAtlas = ImageIO.read(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int width = buffer.getWidth() * CHAR_WIDTH;
        int height = buffer.getHeight() * CHAR_HEIGHT;
        this.backBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        setPreferredSize(new Dimension(width, height));
    }

    @Override
    public void render() {
        drawToBackBuffer();
        repaint();
    }

    private void drawToBackBuffer() {
        Graphics2D g2d = backBuffer.createGraphics();
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
        g.drawImage(backBuffer, 0, 0, null);
    }

    private void drawChar(Graphics2D g, int x, int y, Screen.ScreenChar sc) {
        int charCode = sc.getCharacter() & 0xFF;
        int sx = (charCode % 16) * CHAR_WIDTH;
        int sy = (charCode / 16) * CHAR_HEIGHT;

        int dx = x * CHAR_WIDTH;
        int dy = y * CHAR_HEIGHT;

        // Draw background
        g.setColor(sc.getBackground());
        g.fillRect(dx, dy, CHAR_WIDTH, CHAR_HEIGHT);

        // Apply foreground color to 1-bit glyph
        for (int gy = 0; gy < CHAR_HEIGHT; gy++) {
            for (int gx = 0; gx < CHAR_WIDTH; gx++) {
                int pixel = fontAtlas.getRGB(sx + gx, sy + gy) & 0xFFFFFF;
                if (pixel != 0x000000) {
                    g.setColor(sc.getForeground());
                    g.fillRect(dx + gx, dy + gy, 1, 1);
                }
            }
        }
    }

    @Override
    public JPanel getPanel() {
        return this;
    }
}