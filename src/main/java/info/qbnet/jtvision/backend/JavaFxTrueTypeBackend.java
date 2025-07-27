package info.qbnet.jtvision.backend;

import info.qbnet.jtvision.backend.util.ColorUtil;
import info.qbnet.jtvision.core.Screen;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;

import java.io.InputStream;

/**
 * JavaFX-based backend that renders text using a TTF VGA-style font.
 */
public class JavaFxTrueTypeBackend extends AbstractJavaFxBackend {

    private Font font;

    public JavaFxTrueTypeBackend(Screen buffer, int charWidth, int charHeight) {
        super(buffer, charWidth, charHeight);

        // initialization deferred until JavaFX stage is ready
    }

    // drawToCanvas() inherited

    @Override
    protected void initResources() {
        try (InputStream fontStream = getClass().getResourceAsStream("/PxPlus_IBM_VGA_9x16.ttf")) {
            if (fontStream == null) {
                throw new RuntimeException("Font not found: PxPlus_IBM_VGA_9x16.ttf");
            }
            this.font = Font.loadFont(fontStream, 16);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load TTF font", e);
        }
    }

    @Override
    protected void configureGraphics(GraphicsContext gc) {
        gc.setFont(font);
        gc.setFontSmoothingType(FontSmoothingType.LCD);
    }

    @Override
    protected void drawChar(GraphicsContext gc, int x, int y, Screen.ScreenChar sc) {
        double dx = x * getCharWidth();
        double dy = (y + 1) * getCharHeight() - 3; // vertical align

        gc.setFill(ColorUtil.toFx(sc.getBackground()));
        gc.fillRect(dx, dy - getCharHeight() + 3, getCharWidth(), getCharHeight());

        gc.setFill(ColorUtil.toFx(sc.getForeground()));
        gc.fillText(Character.toString(sc.getCharacter()), dx, dy);
    }
}

