package fr.kevernlt.iutgon.gui.jfx;

/**
 * Alternative launcher class for the JavaFX application.
 * Useful when running without modular JavaFX VM options.
 */
public class App {
    /**
     * Main method delegating to {@link BridgeApp#main(String[])}.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        BridgeApp.main(args);
    }
}
