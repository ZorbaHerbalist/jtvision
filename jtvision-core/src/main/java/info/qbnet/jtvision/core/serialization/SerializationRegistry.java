package info.qbnet.jtvision.core.serialization;

import info.qbnet.jtvision.core.app.TBackground;
import info.qbnet.jtvision.core.dialogs.TButton;
import info.qbnet.jtvision.core.dialogs.TDialog;
import info.qbnet.jtvision.core.dialogs.TInputLine;
import info.qbnet.jtvision.core.dialogs.TLabel;
import info.qbnet.jtvision.core.dialogs.TRadioButtons;
import info.qbnet.jtvision.core.dialogs.TStaticText;
import info.qbnet.jtvision.core.menus.TMenuBar;
import info.qbnet.jtvision.core.menus.TMenuBox;
import info.qbnet.jtvision.core.menus.TMenuView;
import info.qbnet.jtvision.core.menus.TStatusLine;
import info.qbnet.jtvision.core.views.TFrame;
import info.qbnet.jtvision.core.views.TGroup;
import info.qbnet.jtvision.core.views.TView;
import info.qbnet.jtvision.core.views.TWindow;

/**
 * Utility class for registering built-in view classes with {@code TStream}.
 */
public final class SerializationRegistry {

    private SerializationRegistry() {
        // Utility class
    }

    /**
     * Registers core dialog, menu, and view classes with {@code TStream}.
     */
    public static void initCoreTypes() {
        TView.registerType();
        TGroup.registerType();
        TWindow.registerType();
        TFrame.registerType();
        TDialog.registerType();
        TStaticText.registerType();
        TInputLine.registerType();
        TButton.registerType();
        TLabel.registerType();
        TRadioButtons.registerType();
        TBackground.registerType();
        TMenuView.registerType();
        TMenuBar.registerType();
        TMenuBox.registerType();
        TStatusLine.registerType();
    }
}
