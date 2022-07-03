/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thebajero.mp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author Sango
 */
public class THEBAJEROMP extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("FXMLPlayer.fxml"));
        
        Scene scene = new Scene(root);
        
        stage.setTitle("THEBAJERO MP");
        stage.getIcons().add(new Image("icons/THEBAJERO MP small for app.png"));
        
        int width = 925;
        int height = 670;
        
        stage.setMaxWidth(width);
        stage.setMaxHeight(height);
        
        stage.setMinWidth(width);
        stage.setMinHeight(height);
        
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
