package info.qbnet.jtvision.backend.factory;

import info.qbnet.jtvision.backend.Backend;
import info.qbnet.jtvision.core.Screen;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import info.qbnet.jtvision.backend.util.ThreadWatcher;
import java.util.function.Function;

/**
 * Generic JavaFX backend factory using constructor injection.
 */
public class JavaFxFactory extends AbstractGuiFactory<JavaFxFactory.FxBackendWithCanvas> {

    public JavaFxFactory(Function<Screen, ? extends FxBackendWithCanvas> constructor) {
        super(constructor);
    }

    @Override
    public void initialize() {
        Platform.startup(() -> {
            // no-op, just initialize JavaFX runtime
        });
    }

    @Override
    public FxBackendWithCanvas createBackend(Screen buffer) {
        Thread mainThread = Thread.currentThread();

        AtomicReference<FxBackendWithCanvas> backendRef = new AtomicReference<>();

        return createAndInitialize(buffer, (backend, latch) ->
                Platform.runLater(() -> {
                    backendRef.set(backend);

                    StackPane root = new StackPane(backend.getCanvas());
                    Scene scene = new Scene(root);
                    Stage stage = new Stage();
                    stage.setTitle("Console (Library: JavaFX, Renderer: " +
                            backend.getClass().getSimpleName() + ")");
                    stage.setScene(scene);
                    stage.setOnCloseRequest(event -> {
                        event.consume();
                        Platform.exit();
                        System.exit(0);
                    });
                    stage.show();

                    ThreadWatcher.onTermination(mainThread, () ->
                            Platform.runLater(() -> {
                                stage.close();
                                Platform.exit();
                            }));

                    latch.countDown();
                }));
    }

    public interface FxBackendWithCanvas extends Backend {
        Canvas getCanvas();
    }
}
