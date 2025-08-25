package info.qbnet.jtvision.core.event;

import com.badlogic.gdx.Input;
import info.qbnet.jtvision.backend.AbstractJavaFxBackend;
import info.qbnet.jtvision.backend.AbstractLibGdxBackend;
import info.qbnet.jtvision.backend.AbstractSwingBackend;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class KeyCodeMapperTest {

    @Test
    void testLetterKeyMappingConsistency() {
        int swing = AbstractSwingBackend.mapKeyCode(java.awt.event.KeyEvent.VK_A);
        int jfx = AbstractJavaFxBackend.mapKeyCode(javafx.scene.input.KeyCode.A.getCode());
        int gdx = AbstractLibGdxBackend.mapKeyCode(Input.Keys.A);
        assertEquals(65, swing);
        assertEquals(swing, jfx);
        assertEquals(swing, gdx);
    }

    @Test
    void testFunctionKeyMappingConsistency() {
        int swing = AbstractSwingBackend.mapKeyCode(java.awt.event.KeyEvent.VK_F1);
        int jfx = AbstractJavaFxBackend.mapKeyCode(javafx.scene.input.KeyCode.F1.getCode());
        int gdx = AbstractLibGdxBackend.mapKeyCode(Input.Keys.F1);
        assertEquals(swing, jfx);
        assertEquals(swing, gdx);
    }

    @Test
    void testModifierFlags() {
        int base = AbstractSwingBackend.mapKeyCode(java.awt.event.KeyEvent.VK_B);
        int coded = KeyCodeMapper.applyModifiers(base, true, true, false);
        assertTrue((coded & KeyCodeMapper.SHIFT) != 0);
        assertTrue((coded & KeyCodeMapper.CTRL) != 0);
        assertEquals(0, coded & KeyCodeMapper.ALT);
    }
}
