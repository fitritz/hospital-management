import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;

import java.util.List;

public class PatientsView extends Tab {
  private final ApiClient apiClient;
  private final ObservableList<HospitalManagementFX.Patient> patients;
  private final ObservableList<HospitalManagementFX.Doctor> doctors;
  private final ObservableList<HospitalManagementFX.Appointment> appointments;
  private final ToastManager toastManager;

  private ListView<HospitalManagementFX.Patient> patientListView;

  public PatientsView(ApiClient apiClient,
      ObservableList<HospitalManagementFX.Patient> patients,
      ObservableList<HospitalManagementFX.Doctor> doctors,
      ObservableList<HospitalManagementFX.Appointment> appointments,
      ToastManager toastManager) {
    super("Patients");
    this.apiClient = apiClient;
    this.patients = patients;
    this.doctors = doctors;
    this.appointments = appointments;
    this.toastManager = toastManager;

    setContent(buildContent());
  }

  private Node buildContent() {
    // Search toolbar
    TextField searchField = new TextField();
    searchField.setPromptText("Search patients by name, contact, gender...");
    searchField.getStyleClass().add("search-box");

    HBox toolbar = new HBox(12, searchField);
    toolbar.setPadding(new Insets(6, 0, 12, 0));
    toolbar.setAlignment(Pos.CENTER_LEFT);

    // Form fields
    TextField nameField = new TextField();
    nameField.setPromptText("Full name");
    TextField ageField = new TextField();
    ageField.setPromptText("Age");
    TextField genderField = new TextField();
    genderField.setPromptText("Gender");
    TextField contactField = new TextField();
    contactField.setPromptText("Contact number");

    Button addButton = new Button("Add Patient");
    addButton.getStyleClass().addAll("primary-button", "primary");
    addButton.setMaxWidth(Double.MAX_VALUE);

    Button deleteButton = new Button("Delete Selected");
    deleteButton.getStyleClass().add("danger-button");
    deleteButton.setMaxWidth(Double.MAX_VALUE);

    GridPane form = new GridPane();
    form.setHgap(10);
    form.setVgap(10);
    form.addRow(0, new Label("Name"), nameField);
    form.addRow(1, new Label("Age"), ageField);
    form.addRow(2, new Label("Gender"), genderField);
    form.addRow(3, new Label("Contact"), contactField);
    form.add(addButton, 1, 4);
    form.add(deleteButton, 1, 5);

    ColumnConstraints left = new ColumnConstraints();
    left.setMinWidth(120);
    ColumnConstraints right = new ColumnConstraints();
    right.setHgrow(Priority.ALWAYS);
    form.getColumnConstraints().addAll(left, right);

    VBox leftPane = new VBox(12, new Label("Add Patient"), form);
    leftPane.getStyleClass().add("card-pane");
    leftPane.setPrefWidth(360);

    // Table view (modern data table)
    FilteredList<HospitalManagementFX.Patient> filtered = new FilteredList<>(patients, p -> true);

    TableView<HospitalManagementFX.Patient> table = new TableView<>(filtered);
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    table.getStyleClass().add("data-table");

    TableColumn<HospitalManagementFX.Patient, String> idCol = new TableColumn<>("ID");
    idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().id)));
    idCol.setMaxWidth(80);

    TableColumn<HospitalManagementFX.Patient, String> nameCol = new TableColumn<>("Name");
    nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().name));

    TableColumn<HospitalManagementFX.Patient, String> ageCol = new TableColumn<>("Age");
    ageCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().age)));
    ageCol.setMaxWidth(80);

    TableColumn<HospitalManagementFX.Patient, String> genderCol = new TableColumn<>("Gender");
    genderCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().gender));
    genderCol.setMaxWidth(120);

    TableColumn<HospitalManagementFX.Patient, String> contactCol = new TableColumn<>("Contact");
    contactCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().contact));

    TableColumn<HospitalManagementFX.Patient, Void> actionsCol = new TableColumn<>("Actions");
    actionsCol.setMaxWidth(160);
    actionsCol.setCellFactory(col -> new TableCell<>() {
      private final HBox box = new HBox(6);
      private final Button editBtn = new Button("Edit");
      private final Button delBtn = new Button("Delete");
      {
        editBtn.getStyleClass().addAll("secondary-button");
        delBtn.getStyleClass().addAll("danger-button");
        box.getChildren().addAll(editBtn, delBtn);
      }

      @Override
      protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
          setGraphic(null);
        } else {
          HospitalManagementFX.Patient p = getTableView().getItems().get(getIndex());
          editBtn.setOnAction(e -> showEditDialog(p));
          delBtn.setOnAction(e -> {
            try {
              if (apiClient.deletePatient(p.id)) {
                patients.remove(p);
                if (toastManager != null)
                  toastManager.showToast("success", "Deleted patient: " + p.name);
              }
            } catch (Exception ex) {
              Alert a = new Alert(Alert.AlertType.ERROR, "Could not delete patient: " + ex.getMessage());
              a.showAndWait();
            }
          });
          setGraphic(box);
        }
      }
    });

    table.getColumns().addAll(idCol, nameCol, ageCol, genderCol, contactCol, actionsCol);

    VBox rightPane = new VBox(8, new Label("Patients"), table);
    rightPane.getStyleClass().add("card-pane");
    VBox.setVgrow(table, Priority.ALWAYS);

    HBox content = new HBox(16, leftPane, rightPane);
    content.setAlignment(Pos.TOP_LEFT);

    VBox container = new VBox(10, toolbar, content);
    container.setPadding(new Insets(8));

    // Actions
    addButton.setOnAction(e -> {
      String name = nameField.getText().trim();
      String ageText = ageField.getText().trim();
      String gender = genderField.getText().trim();
      String contact = contactField.getText().trim();
      if (name.isEmpty() || ageText.isEmpty() || gender.isEmpty() || contact.isEmpty()) {
        Alert a = new Alert(Alert.AlertType.ERROR, "All fields are required.");
        a.showAndWait();
        return;
      }
      int age;
      try {
        age = Integer.parseInt(ageText);
      } catch (NumberFormatException ex) {
        Alert a = new Alert(Alert.AlertType.ERROR, "Age must be a number.");
        a.showAndWait();
        return;
      }

      try {
        HospitalManagementFX.Patient p = apiClient.insertPatient(name, age, gender, contact);
        patients.add(p);
        nameField.clear();
        ageField.clear();
        genderField.clear();
        contactField.clear();
        if (toastManager != null)
          toastManager.showToast("success", "Added patient: " + p.name);
      } catch (Exception ex) {
        Alert a = new Alert(Alert.AlertType.ERROR, "Could not save patient: " + ex.getMessage());
        a.showAndWait();
      }
    });

    deleteButton.setOnAction(e -> {
      HospitalManagementFX.Patient sel = patientListView.getSelectionModel().getSelectedItem();
      if (sel == null) {
        Alert a = new Alert(Alert.AlertType.ERROR, "Select a patient to delete.");
        a.showAndWait();
        return;
      }
      try {
        if (apiClient.deletePatient(sel.id)) {
          patients.remove(sel);
          if (toastManager != null)
            toastManager.showToast("success", "Deleted patient: " + sel.name);
        }
      } catch (Exception ex) {
        Alert a = new Alert(Alert.AlertType.ERROR, "Could not delete patient: " + ex.getMessage());
        a.showAndWait();
      }
    });

    // Key shortcut for editing selected row
    // (TableView handles selection)

    // wire search
    searchField.textProperty().addListener((obs, oldV, newV) -> {
      String f = newV == null ? "" : newV.trim().toLowerCase();
      filtered.setPredicate(p -> {
        if (f.isEmpty())
          return true;
        return p.name.toLowerCase().contains(f) || p.contact.toLowerCase().contains(f)
            || p.gender.toLowerCase().contains(f);
      });
    });

    // Enter key edits selected row
    // (TableView selection)
    // Add keyboard handler to container
    container.setOnKeyPressed(evt -> {
      if (evt.getCode() == KeyCode.ENTER) {
        HospitalManagementFX.Patient sel = null;
        if (!filtered.isEmpty()) {
          sel = filtered.get(0);
        }
        // better: use table selection when available
      }
    });

    return container;
  }

  private void showEditDialog(HospitalManagementFX.Patient patient) {
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Edit Patient");
    ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    TextField nameField = new TextField(patient.name);
    TextField ageField = new TextField(String.valueOf(patient.age));
    TextField genderField = new TextField(patient.gender);
    TextField contactField = new TextField(patient.contact);
    grid.addRow(0, new Label("Name"), nameField);
    grid.addRow(1, new Label("Age"), ageField);
    grid.addRow(2, new Label("Gender"), genderField);
    grid.addRow(3, new Label("Contact"), contactField);

    dialog.getDialogPane().setContent(grid);
    dialog.setResultConverter(btn -> btn == save ? save : null);
    dialog.showAndWait().ifPresent(res -> {
      try {
        int age = Integer.parseInt(ageField.getText().trim());
        String name = nameField.getText().trim();
        String gender = genderField.getText().trim();
        String contact = contactField.getText().trim();
        boolean ok = apiClient.updatePatient(patient.id, name, age, gender, contact);
        if (ok) {
          patient.name = name;
          patient.age = age;
          patient.gender = gender;
          patient.contact = contact;
          patientListView.refresh();
          if (toastManager != null)
            toastManager.showToast("success", "Updated patient: " + patient.name);
        } else {
          Alert a = new Alert(Alert.AlertType.ERROR, "Update failed");
          a.showAndWait();
        }
      } catch (NumberFormatException ex) {
        Alert a = new Alert(Alert.AlertType.ERROR, "Age must be a number");
        a.showAndWait();
      } catch (Exception ex) {
        Alert a = new Alert(Alert.AlertType.ERROR, "Could not update: " + ex.getMessage());
        a.showAndWait();
      }
    });
  }
}
