package info.qbnet.jtvision.backend.factory;

import info.qbnet.jtvision.core.Screen;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;

import java.util.function.Function;
import java.util.concurrent.CountDownLatch;

/**
 * Generic JavaFX backend factory using constructor injection.
 */
public class JavaFxFactory extends Factory<GuiComponent<Canvas>> {

    public JavaFxFactory(Function<Screen, ? extends GuiComponent<Canvas>> constructor) {
        super(constructor, "JavaFX");
    }

    @Override
    public void initialize() {
        Platform.startup(() -> {
            // no-op, just initialize JavaFX runtime
        });
    }

    @Override
    protected void initializeBackend(GuiComponent<Canvas> backend,
                                    int pixelWidth, int pixelHeight,
                                    CountDownLatch latch, Thread mainThread) {
        Platform.runLater(() -> {
            FactoryConfig config = createFactoryConfig(backend);

            Canvas canvas = backend.getNativeComponent();
            StackPane root = new StackPane(canvas);
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle(config.getTitle());
            stage.setScene(scene);
            stage.setOnCloseRequest(event -> {
                event.consume();
                Platform.exit();
                System.exit(0);
            });
            stage.show();

            setupThreadCleanup(mainThread, () ->
                    Platform.runLater(() -> {
                        stage.close();
                        Platform.exit();
                    }));

            latch.countDown();
        });
    }
}
