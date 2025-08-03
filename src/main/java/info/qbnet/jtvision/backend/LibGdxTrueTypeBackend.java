package info.qbnet.jtvision.backend;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import info.qbnet.jtvision.backend.util.ColorUtil;
import info.qbnet.jtvision.util.Screen;
import info.qbnet.jtvision.util.buffer.CharacterBuffer.CharacterCell;

/**
 * LibGDX backend rendering console using TrueType font.
 */
public class LibGdxTrueTypeBackend extends AbstractLibGdxBackend {

    private BitmapFont font;

    public LibGdxTrueTypeBackend(Screen screen, int charWidth, int charHeight) {
        super(screen, charWidth, charHeight);
    }

    @Override
    protected void initializeResources() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("PxPlus_IBM_VGA_9x16.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = 16;
        parameter.flip = false;
        font = generator.generateFont(parameter);
        generator.dispose();
    }

    @Override
    protected void drawGlyph(SpriteBatch batch, CharacterCell ch, int x, int pixelY) {
        font.setColor(ColorUtil.toGdx(ch.foreground()));
        font.draw(batch, String.valueOf(ch.character()), x * getCellWidth(), pixelY + getCellHeight() - 2);
    }

    @Override
    protected void disposeResources() {
        font.dispose();
    }
}
