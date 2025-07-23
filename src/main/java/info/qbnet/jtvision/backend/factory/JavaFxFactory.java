package info.qbnet.jtvision.backend.factory;

import info.qbnet.jtvision.backend.Backend;
import info.qbnet.jtvision.backend.Screen;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;

import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

/**
 * Generic JavaFX backend factory using constructor injection.
 */
public class JavaFxFactory implements Factory {

    private final Function<Screen, ? extends FxBackendWithCanvas> constructor;
    private Backend backend;

    public JavaFxFactory(Function<Screen, ? extends FxBackendWithCanvas> constructor) {
        this.constructor = constructor;
    }

    @Override
    public void initialize() {
        new JFXPanel();
    }

    @Override
    public Backend createBackend(Screen buffer) {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            FxBackendWithCanvas backendWithCanvas = constructor.apply(buffer);
            backend = backendWithCanvas;

            StackPane root = new StackPane(backendWithCanvas.getCanvas());
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Console (Library: JavaFX, Renderer: " + backend.getClass().getSimpleName() + ")");
            stage.setScene(scene);
            stage.show();

            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return backend;
    }

    public interface FxBackendWithCanvas extends Backend {
        Canvas getCanvas();
    }
}
