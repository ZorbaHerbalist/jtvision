package info.qbnet.jtvision.backend;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import info.qbnet.jtvision.backend.util.ColorUtil;
import info.qbnet.jtvision.util.Screen;

/**
 * LibGDX backend rendering console using a bitmap font texture.
 */
public class LibGdxBitmapBackend extends AbstractLibGdxBackend {
    private static final int CHARS_PER_ROW = 16;

    private Texture fontTexture;

    public LibGdxBitmapBackend(Screen screen, int charWidth, int charHeight) {
        super(screen, charWidth, charHeight);
    }

    @Override
    protected void initializeResources() {
        fontTexture = new Texture(Gdx.files.internal("font_white_8x16_2.png"));
        fontTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    @Override
    protected void drawGlyph(SpriteBatch batch, Screen.CharacterCell ch, int x, int pixelY) {
        batch.setColor(ColorUtil.toGdx(ch.getForeground()));
        int charCode = ch.getCharacter();
        int sourceX = (charCode % CHARS_PER_ROW) * getCellWidth();
        int sourceY = (charCode / CHARS_PER_ROW) * getCellHeight();
        batch.draw(fontTexture, x * getCellWidth(), pixelY, getCellWidth(), getCellHeight(),
                sourceX, sourceY, getCellWidth(), getCellHeight(), false, false);
    }

    @Override
    protected void disposeResources() {
        fontTexture.dispose();
    }
}
