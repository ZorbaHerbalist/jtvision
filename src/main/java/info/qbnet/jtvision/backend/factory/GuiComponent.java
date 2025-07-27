package info.qbnet.jtvision.backend.factory;

import info.qbnet.jtvision.backend.Backend;

/**
 * Unified interface for GUI components that can be embedded in different UI frameworks.
 * This interface abstracts away the framework-specific component types while providing
 * a common backend interface for console rendering.
 *
 * <h2>Framework-specific Component Types</h2>
 *
 * <p>The generic type {@code T} represents the native GUI component type for each framework:</p>
 *
 * <h3>JavaFX Backends (T = {@link javafx.scene.canvas.Canvas})</h3>
 * <ul>
 *   <li><strong>Component Type:</strong> {@code Canvas} - JavaFX drawing surface</li>
 *   <li><strong>Rendering Method:</strong> Direct pixel manipulation using {@code GraphicsContext}</li>
 *   <li><strong>Threading:</strong> Must render on JavaFX Application Thread via {@code Platform.runLater()}</li>
 *   <li><strong>Embedding:</strong> Canvas can be added to any JavaFX {@code Parent} node (Scene, StackPane, etc.)</li>
 *   <li><strong>Use Case:</strong> High-performance 2D graphics with hardware acceleration support</li>
 * </ul>
 *
 * <h3>Swing Backends (T = {@link javax.swing.JPanel})</h3>
 * <ul>
 *   <li><strong>Component Type:</strong> {@code JPanel} - Swing container component</li>
 *   <li><strong>Rendering Method:</strong> Double-buffered painting via {@code paintComponent()} override</li>
 *   <li><strong>Threading:</strong> Must render on Event Dispatch Thread via {@code SwingUtilities.invokeLater()}</li>
 *   <li><strong>Embedding:</strong> JPanel can be added to any Swing container (JFrame, JDialog, etc.)</li>
 *   <li><strong>Use Case:</strong> Traditional desktop applications with standard Swing look-and-feel</li>
 * </ul>
 *
 * <h3>LibGDX Backends (T = {@link com.badlogic.gdx.ApplicationAdapter})</h3>
 * <ul>
 *   <li><strong>Component Type:</strong> {@code ApplicationAdapter} - LibGDX application lifecycle handler</li>
 *   <li><strong>Rendering Method:</strong> OpenGL-based rendering with {@code SpriteBatch} and textures</li>
 *   <li><strong>Threading:</strong> Renders on LibGDX render thread via {@code Gdx.app.postRunnable()}</li>
 *   <li><strong>Embedding:</strong> Runs as standalone application window managed by LWJGL</li>
 *   <li><strong>Use Case:</strong> High-performance gaming applications with cross-platform OpenGL support</li>
 * </ul>
 *
 * <h2>Usage Patterns</h2>
 *
 * <h3>Framework Integration</h3>
 * <pre>{@code
 * // JavaFX Integration
 * GuiComponent<Canvas> fxBackend = factory.createBackend(screen);
 * Canvas canvas = fxBackend.getNativeComponent();
 * StackPane root = new StackPane(canvas);
 * Scene scene = new Scene(root);
 *
 * // Swing Integration
 * GuiComponent<JPanel> swingBackend = factory.createBackend(screen);
 * JPanel panel = swingBackend.getNativeComponent();
 * JFrame frame = new JFrame();
 * frame.setContentPane(panel);
 *
 * // LibGDX Integration
 * GuiComponent<ApplicationAdapter> gdxBackend = factory.createBackend(screen);
 * ApplicationAdapter adapter = gdxBackend.getNativeComponent();
 * new LwjglApplication(adapter, config);
 * }</pre>
 *
 * <h3>Common Backend Operations</h3>
 * <pre>{@code
 * // All backends support these common operations regardless of framework:
 * backend.renderScreen();           // Trigger screen refresh
 * backend.afterInitialization();   // Post-creation setup
 * int cellWidth = backend.getCharWidth();   // Get character cell dimensions
 * int cellHeight = backend.getCharHeight();
 * }</pre>
 *
 * <h2>Thread Safety Considerations</h2>
 *
 * <table border="1">
 * <tr><th>Framework</th><th>UI Thread</th><th>Render Method</th><th>Thread Safety</th></tr>
 * <tr><td>JavaFX</td><td>JavaFX Application Thread</td><td>Platform.runLater()</td><td>Canvas operations must be on UI thread</td></tr>
 * <tr><td>Swing</td><td>Event Dispatch Thread</td><td>SwingUtilities.invokeLater()</td><td>Component updates must be on EDT</td></tr>
 * <tr><td>LibGDX</td><td>Render Thread</td><td>Gdx.app.postRunnable()</td><td>OpenGL context bound to render thread</td></tr>
 * </table>
 *
 * <h2>Performance Characteristics</h2>
 *
 * <ul>
 * <li><strong>JavaFX:</strong> Hardware accelerated, good for smooth animations and effects</li>
 * <li><strong>Swing:</strong> CPU-based rendering, reliable but potentially slower for complex graphics</li>
 * <li><strong>LibGDX:</strong> OpenGL-based, highest performance for gaming and real-time applications</li>
 * </ul>
 *
 * @param <T> the type of native GUI component specific to the UI framework:
 *           {@code Canvas} for JavaFX, {@code JPanel} for Swing, {@code ApplicationAdapter} for LibGDX
 *
 * @see info.qbnet.jtvision.backend.AbstractJavaFxBackend
 * @see info.qbnet.jtvision.backend.AbstractSwingBackend
 * @see info.qbnet.jtvision.backend.AbstractLibGdxBackend
 * @see info.qbnet.jtvision.backend.factory.JavaFxFactory
 * @see info.qbnet.jtvision.backend.factory.SwingFactory
 * @see info.qbnet.jtvision.backend.factory.LibGdxFactory
 *
 */
public interface GuiComponent<T> extends Backend {

    /**
     * Gets the native GUI component for embedding in the UI framework.
     *
     * <p>The returned component type depends on the framework implementation:</p>
     * <ul>
     * <li><strong>JavaFX:</strong> Returns {@code Canvas} for direct pixel drawing</li>
     * <li><strong>Swing:</strong> Returns {@code JPanel} for container-based rendering</li>
     * <li><strong>LibGDX:</strong> Returns {@code ApplicationAdapter} for OpenGL rendering lifecycle</li>
     * </ul>
     *
     * <p><strong>Threading Note:</strong> The returned component should only be manipulated
     * on the appropriate UI thread for the framework. Cross-thread access may result in
     * {@code IllegalStateException} or rendering artifacts.</p>
     *
     * <p><strong>Lifecycle:</strong> The component is typically created during backend initialization
     * and remains valid until the backend is disposed. Callers should not dispose or modify
     * the component directly.</p>
     *
     * @return the native GUI component with framework-specific strong typing
     * @throws IllegalStateException if called before backend initialization is complete
     *
     * @see #afterInitialization()
     * @see #renderScreen()
     */
    T getUIComponent();
}
