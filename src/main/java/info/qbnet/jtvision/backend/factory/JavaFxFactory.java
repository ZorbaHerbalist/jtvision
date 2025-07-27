package info.qbnet.jtvision.backend.factory;

import info.qbnet.jtvision.core.Screen;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;
import java.util.concurrent.CountDownLatch;

/**
 * Generic JavaFX backend factory using constructor injection.
 */
public class JavaFxFactory extends Factory<GuiComponent<Canvas>> {

    private static final Logger log = LoggerFactory.getLogger(JavaFxFactory.class);

    public JavaFxFactory(Function<Screen, ? extends GuiComponent<Canvas>> constructor) {
        super(constructor, "JavaFX");
        log.debug("JavaFxFactory created");
    }

    @Override
    public void initialize() {
        log.info("Starting JavaFX platform");
        Platform.startup(() -> {
            // no-op, just initialize JavaFX runtime
        });
    }

    @Override
    protected GuiComponent<Canvas> initializeBackend(Screen screen,
                                                    CountDownLatch latch,
                                                    Thread mainThread) {
        GuiComponent<Canvas> backend = constructor.apply(screen);

        log.info("Starting JavaFX backend");

        Platform.runLater(() -> {
            log.debug("Creating JavaFX stage");
            Canvas canvas = backend.getUIComponent();
            StackPane root = new StackPane(canvas);
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle(createWindowTitle(backend));
            stage.setScene(scene);
            stage.setOnCloseRequest(event -> {
                event.consume();
                log.debug("Closing JavaFX platform");
                Platform.exit();
                System.exit(0);
            });
            stage.show();
            log.debug("Stage shown");

            backend.afterInitialization();

            setupThreadCleanup(mainThread, () ->
                    Platform.runLater(() -> {
                        log.debug("Closing JavaFX stage");
                        stage.close();
                        Platform.exit();
                    }));

            latch.countDown();
        });

        return backend;
    }
}
