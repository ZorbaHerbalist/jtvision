package info.qbnet.jtvision.backend.factory;

import info.qbnet.jtvision.backend.Backend;

/**
 * Unified interface for GUI components that can be embedded in different UI frameworks.
 * This replaces the separate FxBackendWithCanvas, LibGdxBackendWithAdapter, and SwingBackendWithPanel interfaces.
 */
public interface GuiComponent extends Backend {
    
    /**
     * Gets the native GUI component for embedding in the UI framework.
     * The return type varies by implementation:
     * - JavaFX: Canvas
     * - Swing: JPanel  
     * - LibGDX: ApplicationAdapter
     * 
     * @return the native GUI component
     */
    Object getNativeComponent();
    
}