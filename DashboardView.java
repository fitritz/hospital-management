import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.Random;

public class DashboardView extends HBox {
  private final int[] sampleTrend = new int[20];

  public DashboardView(ObservableList<?> patients, ObservableList<?> doctors, ObservableList<?> appointments) {
    setSpacing(12);
    setPadding(new Insets(12, 0, 12, 0));
    setAlignment(Pos.CENTER_LEFT);

    // sample trend data
    Random r = new Random(123);
    for (int i = 0; i < sampleTrend.length; i++)
      sampleTrend[i] = 60 + r.nextInt(40) - i / 3;

    StatCard pCard = new StatCard("Patients", patients.size(), sampleTrend, Color.web("#06B6D4"));
    StatCard dCard = new StatCard("Doctors", doctors.size(), sampleTrend, Color.web("#0F172A"));
    StatCard aCard = new StatCard("Appointments", appointments.size(), sampleTrend, Color.web("#22C55E"));

    getChildren().addAll(pCard, dCard, aCard);

    patients.addListener((ListChangeListener<Object>) c -> pCard.animateTo(patients.size()));
    doctors.addListener((ListChangeListener<Object>) c -> dCard.animateTo(doctors.size()));
    appointments.addListener((ListChangeListener<Object>) c -> aCard.animateTo(appointments.size()));

    // initial small entrance animation
    Timeline entrance = new Timeline(new KeyFrame(Duration.millis(260), e -> {
      /* no-op placeholder */ }));
    entrance.play();
  }

  private VBox createStatCard(String title, Label valueLbl) {
    Label titleLbl = new Label(title);
    titleLbl.setFont(Font.font("System", 14));
    titleLbl.setStyle("-fx-text-fill: #4A5A6A; -fx-font-weight: 700;");

    valueLbl.setFont(Font.font("System", 28));
    valueLbl.setStyle("-fx-text-fill: #0D3550; -fx-font-weight: 800;");

    Canvas spark = new Canvas(120, 32);
    drawSparkline(spark.getGraphicsContext2D(), sampleTrend, Color.web("#0B74FF"));

    VBox box = new VBox(6, titleLbl, valueLbl, spark);
    box.setPadding(new Insets(12));
    box.getStyleClass().addAll("stat-card", "card-pane");
    box.setPrefWidth(220);

    return box;
  }

  private void animateLabelTo(Label label, int target) {
    int current = 0;
    try {
      current = Integer.parseInt(label.getText());
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
      tl.getKeyFrames().add(new KeyFrame(Duration.millis(i * stepTime), ev -> label.setText(String.valueOf(value))));
    }
    tl.play();
  }

  public void addActivity(String message) {
    // lightweight hook for recent activity; expand later
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
