import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Avatar extends StackPane {
  public Avatar(String name, double size) {
    setPrefSize(size, size);
    setMinSize(size, size);
    setMaxSize(size, size);
    String initials = "";
    if (name != null && !name.isEmpty()) {
      String[] parts = name.trim().split("\\s+");
      if (parts.length >= 2)
        initials = ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
      else
        initials = ("" + parts[0].charAt(0)).toUpperCase();
    }
    Circle c = new Circle(size / 2, Color.web("#E6F0FF"));
    c.setStroke(Color.web("#0B74FF"));
    c.setStrokeWidth(1.5);
    Label lbl = new Label(initials);
    lbl.setStyle("-fx-font-weight: 700; -fx-text-fill: #0B74FF;");
    setAlignment(lbl, Pos.CENTER);
    getChildren().addAll(c, lbl);
  }
}
