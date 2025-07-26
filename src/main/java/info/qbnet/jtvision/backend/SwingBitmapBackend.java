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

    private final BufferedImage fontAtlas;

    public SwingBitmapBackend(Screen buffer, int charWidth, int charHeight) {
        super(buffer, charWidth, charHeight);

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
        int sx = (charCode % 16) * getCharWidth();
        int sy = (charCode / 16) * getCharHeight();

        int dx = x * getCharWidth();
        int dy = y * getCharHeight();

        // Draw background
        g.setColor(sc.getBackground());
        g.fillRect(dx, dy, getCharWidth(), getCharHeight());

        // Apply foreground color to 1-bit glyph
        for (int gy = 0; gy < getCharHeight(); gy++) {
            for (int gx = 0; gx < getCharWidth(); gx++) {
                int pixel = fontAtlas.getRGB(sx + gx, sy + gy) & 0xFFFFFF;
                if (pixel != 0x000000) {
                    g.setColor(sc.getForeground());
                    g.fillRect(dx + gx, dy + gy, 1, 1);
                }
            }
        }
    }
}