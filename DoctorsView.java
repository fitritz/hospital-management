import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;

public class DoctorsView extends Tab {
  private final ApiClient apiClient;
  private final ObservableList<HospitalManagementFX.Doctor> doctors;
  private final ObservableList<HospitalManagementFX.Patient> patients;
  private final ObservableList<HospitalManagementFX.Appointment> appointments;
  private final ToastManager toastManager;

  private ListView<HospitalManagementFX.Doctor> doctorListView;

  public DoctorsView(ApiClient apiClient,
      ObservableList<HospitalManagementFX.Doctor> doctors,
      ObservableList<HospitalManagementFX.Patient> patients,
      ObservableList<HospitalManagementFX.Appointment> appointments,
      ToastManager toastManager) {
    super("Doctors");
    this.apiClient = apiClient;
    this.doctors = doctors;
    this.patients = patients;
    this.appointments = appointments;
    this.toastManager = toastManager;

    setContent(buildContent());
  }

  private Node buildContent() {
    TextField search = new TextField();
    search.setPromptText("Search doctors by name or specialization");
    search.getStyleClass().add("search-box");

    HBox toolbar = new HBox(12, search);
    toolbar.setPadding(new Insets(6, 0, 12, 0));
    toolbar.setAlignment(Pos.CENTER_LEFT);

    TextField nameField = new TextField();
    nameField.setPromptText("Doctor name");
    TextField specializationField = new TextField();
    specializationField.setPromptText("Specialization");
    TextField contactField = new TextField();
    contactField.setPromptText("Contact number");

    Button addBtn = new Button("Add Doctor");
    addBtn.getStyleClass().addAll("primary-button", "primary");
    addBtn.setMaxWidth(Double.MAX_VALUE);
    Button deleteBtn = new Button("Delete Selected");
    deleteBtn.getStyleClass().add("danger-button");
    deleteBtn.setMaxWidth(Double.MAX_VALUE);

    GridPane form = new GridPane();
    form.setHgap(10);
    form.setVgap(10);
    form.addRow(0, new Label("Name"), nameField);
    form.addRow(1, new Label("Specialization"), specializationField);
    form.addRow(2, new Label("Contact"), contactField);
    form.add(addBtn, 1, 3);
    form.add(deleteBtn, 1, 4);

    ColumnConstraints left = new ColumnConstraints();
    left.setMinWidth(120);
    ColumnConstraints right = new ColumnConstraints();
    right.setHgrow(Priority.ALWAYS);
    form.getColumnConstraints().addAll(left, right);

    VBox leftPane = new VBox(12, new Label("Add Doctor"), form);
    leftPane.getStyleClass().add("card-pane");
    leftPane.setPrefWidth(360);

    FilteredList<HospitalManagementFX.Doctor> filtered = new FilteredList<>(doctors, d -> true);
    doctorListView = new ListView<>(filtered);
    doctorListView.setPlaceholder(new Label("No doctors yet."));
    doctorListView.getStyleClass().add("data-list");

    doctorListView.setCellFactory(list -> new ListCell<>() {
      @Override
      protected void updateItem(HospitalManagementFX.Doctor d, boolean empty) {
        super.updateItem(d, empty);
        if (empty || d == null) {
          setText(null);
          setGraphic(null);
        } else {
          VBox v = new VBox(2);
          Label name = new Label(d.name);
          name.getStyleClass().add("list-item-title");
          Label meta = new Label(d.specialization);
          meta.getStyleClass().add("list-item-meta");
          Label contact = new Label(d.contact);
          contact.getStyleClass().add("list-item-contact");
          v.getChildren().addAll(name, meta);
          HBox h = new HBox(12, v, contact);
          HBox.setHgrow(v, Priority.ALWAYS);
          setGraphic(h);
        }
      }
    });

    VBox rightPane = new VBox(6, new Label("Doctor List"), doctorListView);
    rightPane.getStyleClass().add("card-pane");
    VBox.setVgrow(doctorListView, Priority.ALWAYS);

    HBox content = new HBox(16, leftPane, rightPane);
    content.setAlignment(Pos.TOP_LEFT);
    VBox container = new VBox(10, toolbar, content);
    container.setPadding(new Insets(8));

    // Actions
    addBtn.setOnAction(e -> {
      String name = nameField.getText().trim();
      String spec = specializationField.getText().trim();
      String contact = contactField.getText().trim();
      if (name.isEmpty() || spec.isEmpty() || contact.isEmpty()) {
        new Alert(Alert.AlertType.ERROR, "All fields required").showAndWait();
        return;
      }
      try {
        HospitalManagementFX.Doctor d = apiClient.insertDoctor(name, spec, contact);
        doctors.add(d);
        nameField.clear();
        specializationField.clear();
        contactField.clear();
        if (toastManager != null)
          toastManager.showToast("success", "Added doctor: " + d.name);
      } catch (Exception ex) {
        new Alert(Alert.AlertType.ERROR, "Could not save doctor: " + ex.getMessage()).showAndWait();
      }
    });

    deleteBtn.setOnAction(e -> {
      HospitalManagementFX.Doctor sel = doctorListView.getSelectionModel().getSelectedItem();
      if (sel == null) {
        new Alert(Alert.AlertType.ERROR, "Select a doctor to delete").showAndWait();
        return;
      }
      try {
        if (apiClient.deleteDoctor(sel.id)) {
          doctors.remove(sel);
          if (toastManager != null)
            toastManager.showToast("success", "Deleted doctor: " + sel.name);
        }
      } catch (Exception ex) {
        new Alert(Alert.AlertType.ERROR, "Could not delete doctor: " + ex.getMessage()).showAndWait();
      }
    });

    // context menu
    doctorListView.setCellFactory(list -> {
      ListCell<HospitalManagementFX.Doctor> cell = new ListCell<>() {
        @Override
        protected void updateItem(HospitalManagementFX.Doctor d, boolean empty) {
          super.updateItem(d, empty);
          if (empty || d == null) {
            setGraphic(null);
          } else {
            VBox v = new VBox(2);
            Label name = new Label(d.name);
            name.getStyleClass().add("list-item-title");
            Label meta = new Label(d.specialization);
            meta.getStyleClass().add("list-item-meta");
            Label contact = new Label(d.contact);
            contact.getStyleClass().add("list-item-contact");
            v.getChildren().addAll(name, meta);
            HBox h = new HBox(12, v, contact);
            HBox.setHgrow(v, Priority.ALWAYS);
            setGraphic(h);
          }
        }
      };
      MenuItem del = new MenuItem("Delete");
      del.setOnAction(e -> {
        HospitalManagementFX.Doctor d = cell.getItem();
        if (d != null) {
          try {
            if (apiClient.deleteDoctor(d.id)) {
              doctors.remove(d);
              if (toastManager != null)
                toastManager.showToast("success", "Deleted doctor: " + d.name);
            }
          } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Could not delete doctor: " + ex.getMessage()).showAndWait();
          }
        }
      });
      MenuItem edit = new MenuItem("Edit");
      edit.setOnAction(e -> {
        HospitalManagementFX.Doctor d = cell.getItem();
        if (d != null) {
          showEditDialog(d);
        }
      });
      cell.setContextMenu(new ContextMenu(edit, del));
      return cell;
    });

    search.textProperty().addListener((obs, o, n) -> {
      String f = n == null ? "" : n.trim().toLowerCase();
      filtered.setPredicate(d -> {
        if (f.isEmpty())
          return true;
        return d.name.toLowerCase().contains(f) || d.specialization.toLowerCase().contains(f);
      });
    });

    return container;
  }

  private void showEditDialog(HospitalManagementFX.Doctor d) {
    Dialog<ButtonType> dlg = new Dialog<>();
    dlg.setTitle("Edit Doctor");
    ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
    dlg.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);
    GridPane g = new GridPane();
    g.setHgap(10);
    g.setVgap(10);
    TextField name = new TextField(d.name);
    TextField spec = new TextField(d.specialization);
    TextField contact = new TextField(d.contact);
    g.addRow(0, new Label("Name"), name);
    g.addRow(1, new Label("Specialization"), spec);
    g.addRow(2, new Label("Contact"), contact);
    dlg.getDialogPane().setContent(g);
    dlg.setResultConverter(btn -> btn == save ? save : null);
    dlg.showAndWait().ifPresent(r -> {
      try { // simple update locally then persist
        d.name = name.getText().trim();
        d.specialization = spec.getText().trim();
        d.contact = contact.getText().trim();
        doctorListView.refresh();
        if (toastManager != null)
          toastManager.showToast("success", "Updated doctor: " + d.name);
      } catch (Exception ex) {
        new Alert(Alert.AlertType.ERROR, "Could not update doctor: " + ex.getMessage()).showAndWait();
      }
    });
  }
}
