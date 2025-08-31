package info.qbnet.jtvision.backend;

import info.qbnet.jtvision.util.Screen;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class SwingBackendFocusTraversalTest {

    @Test
    public void focusTraversalDisabled() throws Exception {
        System.setProperty("java.awt.headless", "true");
        Screen screen = new Screen(1, 1);
        SwingBasicBackend backend = new SwingBasicBackend(screen, 8, 16);
        assertFalse(backend.getFocusTraversalKeysEnabled(),
                "Focus traversal keys should be disabled to capture Tab");

        Field field = AbstractSwingBackend.class.getDeclaredField("cursorBlink");
        field.setAccessible(true);
        ScheduledExecutorService executor = (ScheduledExecutorService) field.get(backend);
        executor.shutdownNow();
    }
}
