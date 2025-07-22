package info.qbnet.jtvision.backend;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.concurrent.CountDownLatch;

/**
 * Factory to launch JavaFX TTF font backend synchronously.
 */
public class TTFFontFxBackendFactory implements BackendFactory {

    private Backend backend;

    @Override
    public void initialize() {
        new JFXPanel(); // initializes JavaFX runtime
    }

    @Override
    public Backend createBackend(Screen buffer) {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            TTFFontFxBackend fxBackend = new TTFFontFxBackend(buffer);
            backend = fxBackend;

            StackPane root = new StackPane(fxBackend.getCanvas());
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("DOS Console (JavaFX + TTF Font)");
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
}
