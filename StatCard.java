import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class StatCard extends VBox {
  private final Label valueLabel;
  private final Canvas spark;

  public StatCard(String title, int initialValue, int[] sparkData, Color accent) {
    setSpacing(8);
    setPadding(new Insets(12));
    getStyleClass().addAll("stat-card", "card-pane");
    setPrefWidth(220);

    Label titleLbl = new Label(title);
    titleLbl.getStyleClass().add("stat-label");

    valueLabel = new Label(String.valueOf(initialValue));
    valueLabel.getStyleClass().add("stat-value");
    valueLabel.setFont(Font.font("System", 28));

    spark = new Canvas(120, 36);
    drawSparkline(spark.getGraphicsContext2D(), sparkData, accent);

    StackPane sparkWrap = new StackPane(spark);
    sparkWrap.setAlignment(Pos.CENTER_LEFT);

    getChildren().addAll(titleLbl, valueLabel, sparkWrap);

    // hover lift animation
    setOnMouseEntered(e -> {
      ScaleTransition st = new ScaleTransition(Duration.millis(160), this);
      st.setToX(1.02);
      st.setToY(1.02);
      st.play();
    });
    setOnMouseExited(e -> {
      ScaleTransition st = new ScaleTransition(Duration.millis(160), this);
      st.setToX(1.0);
      st.setToY(1.0);
      st.play();
    });
  }

  public void animateTo(int target) {
    int current = 0;
    try {
      current = Integer.parseInt(valueLabel.getText());
    } catch (Exception ignored) {
    }
    int delta = target - current;
    if (delta == 0)
      return;
    int steps = Math.min(30, Math.abs(delta));
    double stepTime = 300.0 / Math.max(1, steps);
    Timeline tl = new Timeline();
    for (int i = 1; i <= steps; i++) {
      int value = current + (int) Math.round(delta * (i / (double) steps));
      tl.getKeyFrames()
          .add(new KeyFrame(Duration.millis(i * stepTime), ev -> valueLabel.setText(String.valueOf(value))));
    }
    tl.play();
  }

  private void drawSparkline(GraphicsContext gc, int[] data, Color color) {
    double w = gc.getCanvas().getWidth();
    double h = gc.getCanvas().getHeight();
    gc.clearRect(0, 0, w, h);
    gc.setStroke(color);
    gc.setLineWidth(2);
    double step = w / (data.length - 1);
    int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
    for (int v : data) {
      min = Math.min(min, v);
      max = Math.max(max, v);
    }
    double range = Math.max(1, max - min);
    for (int i = 0; i < data.length - 1; i++) {
      double x1 = i * step;
      double y1 = h - ((data[i] - min) / range) * h;
      double x2 = (i + 1) * step;
      double y2 = h - ((data[i + 1] - min) / range) * h;
      gc.strokeLine(x1, y1, x2, y2);
    }
  }
}
