package info.qbnet.jtvision.backend.factory;

import info.qbnet.jtvision.core.Screen;

import javax.swing.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

public class SwingFactory extends Factory<GuiComponent<JPanel>> {

    public SwingFactory(Function<Screen, ? extends GuiComponent<JPanel>> constructor) {
        super(constructor, "Swing");
    }

    @Override
    protected void initializeBackend(GuiComponent<JPanel> backend,
                                    int pixelWidth, int pixelHeight,
                                    CountDownLatch latch, Thread mainThread) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setTitle(createWindowTitle(backend));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(backend.getNativeComponent());
            frame.pack();
            frame.setVisible(true);

            setupThreadCleanup(mainThread, () -> SwingUtilities.invokeLater(frame::dispose));

            latch.countDown();
        });
    }
}
