package info.qbnet.jtvision.backend.factory;

import info.qbnet.jtvision.core.Screen;

import javax.swing.*;
import java.util.function.Function;
import java.util.concurrent.CountDownLatch;

public class SwingFactory extends AbstractGuiFactory<GuiComponent<JPanel>> {

    public SwingFactory(Function<Screen, ? extends GuiComponent<JPanel>> constructor) {
        super(constructor, "Swing");
    }

    @Override
    public GuiComponent<JPanel> createBackend(Screen buffer) {
        Thread mainThread = Thread.currentThread();

        return createAndInitialize(buffer, (backend, latch) ->
                SwingUtilities.invokeLater(() -> {
                    WindowConfig config = createWindowConfig(backend, buffer);
                    
                    JFrame frame = new JFrame();
                    frame.setTitle(config.getTitle());
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    
                    JPanel panel = backend.getNativeComponent();
                    frame.setContentPane(panel);
                    frame.pack();
                    frame.setVisible(true);

                    setupThreadCleanup(mainThread, () -> SwingUtilities.invokeLater(frame::dispose));

                    latch.countDown();
                }));
    }
}
