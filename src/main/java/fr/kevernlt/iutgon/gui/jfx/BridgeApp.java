package fr.kevernlt.iutgon.gui.jfx;

import fr.kevernlt.iutgon.JfxLauncher;
import fr.kevernlt.iutgon.gui.jfx.controller.Controler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main JavaFX application for the Thread Bridge project.
 * Loads the FXML layout and sets up the primary window.
 */
public class BridgeApp extends Application {

    /**
     * Starts the JavaFX application by loading the GUI and showing the main stage.
     *
     * @param primaryStage the primary window stage
     * @throws Exception if FXML cannot be loaded
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(JfxLauncher.class.getResource("Bridge.fxml"));

        Controler controler = new Controler();
        fxmlLoader.setController(controler);

        Scene scene = new Scene(fxmlLoader.load());

        primaryStage.setTitle("Thread Bridge Simulator (JavaFX)");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(e -> {
            controler.stopAnimation();
            System.exit(0);
        });

        primaryStage.show();
    }

    /**
     * Entry point for running the JavaFX application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
