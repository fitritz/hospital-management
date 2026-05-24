import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ModernShell {
  private final VBox sidebar;
  private final HBox topbar;
  private boolean collapsed = false;

  public ModernShell(Stage stage, Node headerContent) {
    sidebar = createSidebar();
    topbar = createTopBar(stage, headerContent);
  }

  public Node getSidebar() {
    return sidebar;
  }

  public Node getTopBar() {
    return topbar;
  }

  private VBox createSidebar() {
    VBox box = new VBox(12);
    box.getStyleClass().add("sidebar");
    box.setPadding(new Insets(20));

    Label brand = new Label("HospitalPro");
    brand.getStyleClass().add("sidebar-brand");

    Separator sep = new Separator();

    Button dash = navButton("Dashboard", "🏠");
    Button patients = navButton("Patients", "🧑‍⚕️");
    Button doctors = navButton("Doctors", "👩‍⚕️");
    Button appts = navButton("Appointments", "📅");
    Button analytics = navButton("Analytics", "📈");
    Button records = navButton("Records", "📂");
    Button settings = navButton("Settings", "⚙️");

    Button collapse = new Button("◀");
    collapse.getStyleClass().add("sidebar-collapse");
    collapse.setOnAction(e -> toggleCollapse());

    VBox.setVgrow(records, Priority.ALWAYS);

    box.getChildren().addAll(brand, sep, dash, patients, doctors, appts, analytics, records, settings, collapse);
    return box;
  }

  private HBox createTopBar(Stage stage, Node headerContent) {
    HBox top = new HBox(12);
    top.getStyleClass().add("topbar");
    top.setPadding(new Insets(12, 20, 12, 20));
    top.setAlignment(Pos.CENTER_LEFT);

    Label welcome = new Label("Welcome, Admin");
    welcome.getStyleClass().add("topbar-welcome");

    StackPane spacer = new StackPane();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    Label datetime = new Label(java.time.LocalDateTime.now().toString());
    datetime.getStyleClass().add("topbar-datetime");

    Button notif = new Button("🔔");
    notif.getStyleClass().add("icon-button");

    top.getChildren().addAll(welcome, spacer, datetime, notif);
    return top;
  }

  private Button navButton(String text, String icon) {
    Button b = new Button(icon + "  " + text);
    b.getStyleClass().add("nav-button");
    b.setMaxWidth(Double.MAX_VALUE);
    return b;
  }

  private void toggleCollapse() {
    collapsed = !collapsed;
    if (collapsed) {
      sidebar.getStyleClass().add("collapsed");
    } else {
      sidebar.getStyleClass().remove("collapsed");
    }
  }
}
