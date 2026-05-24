import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class Skeleton extends StackPane {
  private final Timeline shimmer;

  public Skeleton(double width, double height) {
    Rectangle r = new Rectangle(width, height);
    r.setArcWidth(8);
    r.setArcHeight(8);

    LinearGradient g = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
        new Stop(0, Color.web("#f3f5f8")), new Stop(0.5, Color.web("#e7eefb")), new Stop(1, Color.web("#f3f5f8")));
    r.setFill(g);
    getChildren().add(r);
    setAlignment(r, Pos.CENTER_LEFT);

    shimmer = new Timeline(
        new KeyFrame(Duration.ZERO, new KeyValue(r.translateXProperty(), -width, Interpolator.LINEAR)),
        new KeyFrame(Duration.seconds(1.2), new KeyValue(r.translateXProperty(), width, Interpolator.LINEAR)));
    shimmer.setCycleCount(Timeline.INDEFINITE);
  }

  public void play() {
    shimmer.play();
  }

  public void stop() {
    shimmer.stop();
  }
}
