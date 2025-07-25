package info.qbnet.jtvision.backend.factory;

/**
 * Configuration for window/stage creation across different GUI frameworks.
 */
public class WindowConfig {
    private final String libraryName;
    private final String rendererName;
    private final int width;
    private final int height;
    
    public WindowConfig(String libraryName, String rendererName, int width, int height) {
        this.libraryName = libraryName;
        this.rendererName = rendererName;
        this.width = width;
        this.height = height;
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
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
}