package info.qbnet.jtvision.backend;

import info.qbnet.jtvision.core.Screen;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * A backend that uses a 1-bit monochrome bitmap font atlas and applies color dynamically.
 */
public class SwingBitmapBackend extends AbstractSwingBackend {

    private static final int CHAR_WIDTH = 8;
    private static final int CHAR_HEIGHT = 16;
    private final BufferedImage fontAtlas;

    public SwingBitmapBackend(Screen buffer) {
        super(buffer, CHAR_WIDTH, CHAR_HEIGHT);

        InputStream stream = getClass().getResourceAsStream("/font_white_8x16_2.png");
        if (stream == null) {
            throw new RuntimeException("Missing resource: font_white_8x16_2.png");
        }

        try {
            this.fontAtlas = ImageIO.read(stream);
        } catch (IOException e) {
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
}