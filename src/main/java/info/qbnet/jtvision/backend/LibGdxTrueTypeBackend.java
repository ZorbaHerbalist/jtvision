package info.qbnet.jtvision.backend;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import info.qbnet.jtvision.core.Screen;

/**
 * LibGDX backend rendering console using TrueType font.
 */
public class LibGdxTrueTypeBackend extends ApplicationAdapter implements Backend {
    private static final int CHAR_WIDTH = 11; // increased from 9
    private static final int CHAR_HEIGHT = 20; // increased from 16

    private final Screen screen;
    private BitmapFont font;
    private SpriteBatch batch;
    private Texture pixel;
    private OrthographicCamera camera;
    private ScreenViewport viewport;

    public LibGdxTrueTypeBackend(Screen screen) {
        this.screen = screen;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("PxPlus_IBM_VGA_9x16.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = 20;
        parameter.flip = false; // Use normal orientation
        font = generator.generateFont(parameter);
        generator.dispose();

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixel = new Texture(pixmap);
        pixmap.dispose();

        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        viewport.apply();
        camera.position.set(screen.getWidth() * CHAR_WIDTH / 2f, screen.getHeight() * CHAR_HEIGHT / 2f, 0);
        camera.update();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        for (int y = 0; y < screen.getHeight(); y++) {
            for (int x = 0; x < screen.getWidth(); x++) {
                Screen.ScreenChar ch = screen.getChar(x, y);

                // Flip Y-axis since LibGDX (0,0) is bottom-left
                int drawY = (screen.getHeight() - y - 1) * CHAR_HEIGHT;

                // Draw background
                batch.setColor(convert(ch.getBackground()));
                batch.draw(pixel, x * CHAR_WIDTH, drawY, CHAR_WIDTH, CHAR_HEIGHT);

                // Draw character
                font.setColor(convert(ch.getForeground()));
                font.draw(batch, String.valueOf(ch.getCharacter()), x * CHAR_WIDTH, drawY + CHAR_HEIGHT - 2);
            }
        }

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    private Color convert(java.awt.Color awtColor) {
        return new Color(awtColor.getRed() / 255f, awtColor.getGreen() / 255f, awtColor.getBlue() / 255f, 1f);
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        pixel.dispose();
    }
}