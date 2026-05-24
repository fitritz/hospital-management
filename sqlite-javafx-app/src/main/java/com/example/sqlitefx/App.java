package com.example.sqlitefx;

import com.example.sqlitefx.ui.UserCrudView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

  @Override
  public void start(Stage stage) {
    UserCrudView root = new UserCrudView();
    Scene scene = new Scene(root, 1100, 720);
    scene.getStylesheets().add(App.class.getResource("styles.css").toExternalForm());

    stage.setTitle("JavaFX + SQLite CRUD Demo");
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
