import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ToastManager {
  private final Stage owner;

  public ToastManager(Stage owner) {
    this.owner = owner;
  }

  public void showToast(String type, String message) {
    Popup popup = new Popup();
    HBox box = new HBox();
    box.setPadding(new Insets(10));
    box.setSpacing(8);
    box.setStyle(
        "-fx-background-radius: 10; -fx-background-color: rgba(20,30,40,0.9); -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.24), 10, 0.08, 0, 4);");
    Label lbl = new Label(message);
    lbl.setStyle("-fx-text-fill: white; -fx-font-weight: 600;");
    box.getChildren().add(lbl);
    box.setAlignment(Pos.CENTER_LEFT);
    popup.getContent().add(box);

    // position top-right
    double x = owner.getX() + owner.getWidth() - 360;
    double y = owner.getY() + 24;
    popup.show(owner, x, y);

    Timeline t = new Timeline(
        new KeyFrame(Duration.ZERO, new KeyValue(box.opacityProperty(), 0.0)),
        new KeyFrame(Duration.millis(180), new KeyValue(box.opacityProperty(), 1.0)),
        new KeyFrame(Duration.millis(3400), new KeyValue(box.opacityProperty(), 1.0)),
        new KeyFrame(Duration.millis(3600), new KeyValue(box.opacityProperty(), 0.0)));

    t.setOnFinished(e -> popup.hide());
    t.play();
  }
}
