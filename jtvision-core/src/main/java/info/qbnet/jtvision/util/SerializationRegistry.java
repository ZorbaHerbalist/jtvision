package info.qbnet.jtvision.util;

import info.qbnet.jtvision.views.TBackground;
import info.qbnet.jtvision.views.TButton;
import info.qbnet.jtvision.views.TDialog;
import info.qbnet.jtvision.views.TInputLine;
import info.qbnet.jtvision.views.TListBox;
import info.qbnet.jtvision.views.TLabel;
import info.qbnet.jtvision.views.TRadioButtons;
import info.qbnet.jtvision.views.TStaticText;
import info.qbnet.jtvision.views.TMenuBar;
import info.qbnet.jtvision.views.TMenuBox;
import info.qbnet.jtvision.views.TMenuView;
import info.qbnet.jtvision.views.TStatusLine;
import info.qbnet.jtvision.views.TFrame;
import info.qbnet.jtvision.views.TMenuPopup;
import info.qbnet.jtvision.views.TGroup;
import info.qbnet.jtvision.views.TView;
import info.qbnet.jtvision.views.TWindow;
import info.qbnet.jtvision.views.TScrollBar;
import info.qbnet.jtvision.views.TScroller;

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
        TScrollBar.registerType();
        TScroller.registerType();
        TDialog.registerType();
        TStaticText.registerType();
        TInputLine.registerType();
        TListBox.registerType();
        TButton.registerType();
        TLabel.registerType();
        TRadioButtons.registerType();
        TBackground.registerType();
        TMenuView.registerType();
        TMenuBar.registerType();
        TMenuBox.registerType();
        TMenuPopup.registerType();
        TStatusLine.registerType();
    }
}
