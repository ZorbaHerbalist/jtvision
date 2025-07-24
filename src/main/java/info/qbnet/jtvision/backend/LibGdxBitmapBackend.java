package info.qbnet.jtvision.backend;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import info.qbnet.jtvision.backend.util.ColorUtil;
import info.qbnet.jtvision.core.Screen;

/**
 * LibGDX backend rendering console using a bitmap font texture.
 */
public class LibGdxBitmapBackend extends ApplicationAdapter implements Backend {
    private static final int CHAR_WIDTH = 8;
    private static final int CHAR_HEIGHT = 16;
    private static final int CHARS_PER_ROW = 16; // corrected from 32

    private final Screen screen;
    private SpriteBatch batch;
    private Texture fontTexture;
    private Texture pixel;
    private OrthographicCamera camera;
    private ScreenViewport viewport;

    public LibGdxBitmapBackend(Screen screen) {
        this.screen = screen;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();

        //fontTexture = new Texture(Gdx.files.internal("cp437_9x16.png"));
        fontTexture = new Texture(Gdx.files.internal("font_white_8x16.png"));
        fontTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

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
                int drawY = (screen.getHeight() - y - 1) * CHAR_HEIGHT;

                // Draw background
                batch.setColor(ColorUtil.toGdx(ch.getBackground()));
                batch.draw(pixel, x * CHAR_WIDTH, drawY, CHAR_WIDTH, CHAR_HEIGHT);

                // Draw foreground character with a separate blend mode
                batch.setColor(ColorUtil.toGdx(ch.getForeground()));
                int charCode = ch.getCharacter();
                int srcX = (charCode % CHARS_PER_ROW) * CHAR_WIDTH;
                int srcY = (charCode / CHARS_PER_ROW) * CHAR_HEIGHT;

                batch.draw(fontTexture, x * CHAR_WIDTH, drawY, CHAR_WIDTH, CHAR_HEIGHT,
                        srcX, srcY, CHAR_WIDTH, CHAR_HEIGHT, false, false); // flip Y=false
            }
        }

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }
    @Override
    public void dispose() {
        batch.dispose();
        fontTexture.dispose();
        pixel.dispose();
    }
}
