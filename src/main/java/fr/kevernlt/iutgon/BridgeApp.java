package fr.kevernlt.iutgon;

import fr.kevernlt.iutgon.bridged.controller.Controler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BridgeApp extends Application{


    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(BridgeApp.class.getResource("Bridge.fxml"));

        Controler controler = new Controler();
        fxmlLoader.setController(controler);

        Scene scene = new Scene(fxmlLoader.load());



        primaryStage.setScene(scene);

        primaryStage.show();
    }
}
