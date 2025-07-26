package info.qbnet.jtvision.backend.factory;

/**
 * Configuration for window/stage creation across different GUI frameworks.
 */
public class WindowConfig {
    private final String libraryName;
    private final String rendererName;
    
    public WindowConfig(String libraryName, String rendererName) {
        this.libraryName = libraryName;
        this.rendererName = rendererName;
    }
    
    public String getTitle() {
        return String.format("Console (Library: %s, Renderer: %s)", libraryName, rendererName);
    }
    
    public String getLibraryName() {
        return libraryName;
    }
    
    public String getRendererName() {
        return rendererName;
    }
}