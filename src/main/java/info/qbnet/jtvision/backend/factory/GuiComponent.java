package info.qbnet.jtvision.backend.factory;

import info.qbnet.jtvision.backend.Backend;

/**
 * Unified interface for GUI components that can be embedded in different UI frameworks.
 * This replaces the separate FxBackendWithCanvas, LibGdxBackendWithAdapter, and SwingBackendWithPanel interfaces.
 * 
 * @param <T> the type of native GUI component (Canvas for JavaFX, JPanel for Swing, ApplicationAdapter for LibGDX)
 */
public interface GuiComponent<T> extends Backend {
    
    /**
     * Gets the native GUI component for embedding in the UI framework.
     * 
     * @return the native GUI component with strong typing
     */
    T getNativeComponent();
    
}