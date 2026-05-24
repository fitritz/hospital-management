import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import javafx.application.Platform;

public class HospitalManagementFX extends Application {

  private static final String DEFAULT_MONGO_URI = "mongodb://localhost:27017";
  private static final String DEFAULT_MONGO_DATABASE = "hospital_management";
  private static final String DEFAULT_PATIENTS_COLLECTION = "patients";
  private static final String DEFAULT_DOCTORS_COLLECTION = "doctors";
  private static final String DEFAULT_APPOINTMENTS_COLLECTION = "appointments";
  private static final int AUTO_REFRESH_SECONDS = 15;
  private static final Map<String, String> DOTENV_VALUES = loadDotEnvValues();
  private final ApiClient apiClient = new ApiClient(
      resolveMongoSetting("hospital.mongo.uri", "MONGO_URI", DEFAULT_MONGO_URI),
      resolveMongoSetting("hospital.mongo.database", "MONGO_DATABASE", DEFAULT_MONGO_DATABASE),
      resolveMongoSetting("hospital.mongo.patientsCollection", "MONGO_PATIENTS_COLLECTION",
          DEFAULT_PATIENTS_COLLECTION),
      resolveMongoSetting("hospital.mongo.doctorsCollection", "MONGO_DOCTORS_COLLECTION",
          DEFAULT_DOCTORS_COLLECTION),
      resolveMongoSetting("hospital.mongo.appointmentsCollection", "MONGO_APPOINTMENTS_COLLECTION",
          DEFAULT_APPOINTMENTS_COLLECTION));
  private ToastManager toastManager;
  private Timeline autoRefreshTimeline;
  private boolean apiUnavailableNotified;
  private DashboardView dashboardView;

  private final ObservableList<Patient> patients = FXCollections.observableArrayList();
  private final ObservableList<Doctor> doctors = FXCollections.observableArrayList();
  private final ObservableList<Appointment> appointments = FXCollections.observableArrayList();

  private ListView<Patient> patientListView;
  private ListView<Doctor> doctorListView;
  private ListView<Appointment> appointmentListView;

  private ComboBox<Patient> patientComboBox;
  private ComboBox<Doctor> doctorComboBox;

  private TextArea statusArea;
  private Label patientCountLabel;
  private Label doctorCountLabel;
  private Label appointmentCountLabel;

  private static String resolveMongoSetting(String propertyName, String envName, String defaultValue) {
    String propertyValue = System.getProperty(propertyName);
    if (propertyValue != null && !propertyValue.isBlank()) {
      return propertyValue;
    }

    String dotenvValue = DOTENV_VALUES.get(propertyName);
    if (dotenvValue == null) {
      dotenvValue = DOTENV_VALUES.get(envName);
    }
    if (dotenvValue != null && !dotenvValue.isBlank()) {
      return dotenvValue;
    }

    String envValue = System.getenv(envName);
    if (envValue != null && !envValue.isBlank()) {
      return envValue;
    }

    return defaultValue;
  }

  private static Map<String, String> loadDotEnvValues() {
    Map<String, String> values = new HashMap<>();
    Path dotenvPath = Path.of(".env");
    if (!Files.exists(dotenvPath)) {
      return values;
    }

    try {
      for (String line : Files.readAllLines(dotenvPath, StandardCharsets.UTF_8)) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
          continue;
        }

        int equalsIndex = trimmed.indexOf('=');
        if (equalsIndex <= 0) {
          continue;
        }

        String key = trimmed.substring(0, equalsIndex).trim();
        String value = trimmed.substring(equalsIndex + 1).trim();
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
          value = value.substring(1, value.length() - 1);
        }
        if (!key.isEmpty()) {
          values.put(key, value);
        }
      }
    } catch (IOException exception) {
      System.out.println("Could not read .env file: " + exception.getMessage());
    }

    return values;
  }

  @Override
  public void start(Stage stage) {
    BorderPane root = new BorderPane();
    root.setPadding(new Insets(16));
    root.getStyleClass().add("app-root");

    Label title = new Label("Hospital Management System");
    title.getStyleClass().add("app-title");

    Label subtitle = new Label("JavaFX Frontend for Patients, Doctors, and Appointments");
    subtitle.getStyleClass().add("app-subtitle");

    VBox header = new VBox(6, title, subtitle);
    header.setPadding(new Insets(0, 0, 16, 0));
    header.getStyleClass().add("hero-panel");

    dashboardView = new DashboardView(patients, doctors, appointments);
    dashboardView.getStyleClass().add("dashboard-row");

    // Modern shell: left sidebar + topbar
    ModernShell shell = new ModernShell(stage, dashboardView);
    root.setLeft(shell.getSidebar());
    root.setTop(shell.getTopBar());

    TabPane tabPane = new TabPane();
    tabPane.getStyleClass().add("main-tabs");
    tabPane.getTabs().addAll(
        new PatientsView(apiClient, patients, doctors, appointments, toastManager),
        new DoctorsView(apiClient, doctors, patients, appointments, toastManager),
        new AppointmentsView(apiClient, patients, doctors, appointments, toastManager),
        createRecordsTab());
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

    root.setCenter(tabPane);

    statusArea = new TextArea();
    statusArea.setEditable(false);
    statusArea.setPrefRowCount(6);
    statusArea.setWrapText(true);
    statusArea.setPromptText("System messages will appear here...");
    statusArea.getStyleClass().add("log-area");

    Label logTitle = new Label("Activity Log");
    logTitle.getStyleClass().add("log-title");

    VBox bottomBox = new VBox(8, logTitle, statusArea);
    bottomBox.setPadding(new Insets(16, 0, 0, 0));
    bottomBox.getStyleClass().add("log-panel");
    root.setBottom(bottomBox);

    Scene scene = new Scene(root, 1140, 760);
    // Load modular theme and component stylesheets (phase 0)
    String[] sheets = new String[] {
        "theme/base.css",
        "theme/tokens.css",
        "components/buttons.css",
        "components/cards.css",
        "HospitalManagementFX.css"
    };

    for (String s : sheets) {
      URL url = getClass().getResource(s);
      if (url != null) {
        scene.getStylesheets().add(url.toExternalForm());
      } else {
        // fallback to filesystem path (project root)
        try {
          File f = new File(s);
          if (f.exists()) {
            scene.getStylesheets().add(f.toURI().toURL().toExternalForm());
          } else {
            System.out.println("Stylesheet not found: " + s);
          }
        } catch (MalformedURLException mue) {
          System.out.println("Bad stylesheet URL: " + s + " -> " + mue.getMessage());
        }
      }
    }
    stage.setTitle("Hospital Management System");
    stage.setScene(scene);
    stage.show();
    // initialize toast manager
    toastManager = new ToastManager(stage);
    playIntroAnimation(shell.getTopBar(), tabPane, bottomBox);

    loadDataFromMongo(true);
    startAutoRefresh();

    updateDashboard();
    log("Application started.");
    log("Connected MongoDB: " + maskMongoUri(apiClient.getBaseUrl()));
  }

  private static String maskMongoUri(String uri) {
    if (uri == null || uri.isBlank())
      return "(unknown)";
    int schemeEnd = uri.indexOf("://");
    if (schemeEnd >= 0) {
      int at = uri.indexOf('@', schemeEnd + 3);
      if (at > 0) {
        // keep scheme and host, replace userinfo with ****
        return uri.substring(0, schemeEnd + 3) + "****@" + uri.substring(at + 1);
      }
    }
    return uri;
  }

  private Tab createPatientsTab() {
    // toolbar and search
    TextField searchField = new TextField();
    searchField.setPromptText("Search patients by name, contact, or gender");
    searchField.getStyleClass().add("search-box");

    Button newPatientFab = new Button("+");
    newPatientFab.getStyleClass().add("fab");
    newPatientFab.setTooltip(new Tooltip("Quick add patient"));
    newPatientFab.setOnAction(e -> {
      // focus first input in the form by requesting the scene lookup later
    });

    HBox toolbar = new HBox(10, searchField);
    toolbar.getStyleClass().add("top-toolbar");
    toolbar.setAlignment(Pos.CENTER_LEFT);
    TextField nameField = new TextField();
    nameField.setPromptText("Patient name");

    TextField ageField = new TextField();
    ageField.setPromptText("Age");

    TextField genderField = new TextField();
    genderField.setPromptText("Gender");

    TextField contactField = new TextField();
    contactField.setPromptText("Contact number");

    Button addButton = new Button("Add Patient");
    addButton.setMaxWidth(Double.MAX_VALUE);
    addButton.getStyleClass().add("primary-button");
    addButton.setTooltip(new Tooltip("Add a new patient"));

    // use a filtered view so search updates instantly
    javafx.collections.transformation.FilteredList<Patient> filteredPatients = new javafx.collections.transformation.FilteredList<>(
        patients, p -> true);
    patientListView = new ListView<>(filteredPatients);
    patientListView.setPlaceholder(new Label("No patients added yet."));
    patientListView.getStyleClass().add("data-list");
    patientListView.setCellFactory(list -> new ListCell<>() {
      @Override
      protected void updateItem(Patient patient, boolean empty) {
        super.updateItem(patient, empty);
        if (empty || patient == null) {
          setText(null);
          setGraphic(null);
        } else {
          Label nameLbl = new Label(patient.name);
          nameLbl.getStyleClass().add("list-item-title");

          Label meta = new Label("Age " + patient.age + " • " + patient.gender);
          meta.getStyleClass().add("list-item-meta");

          VBox left = new VBox(2, nameLbl, meta);

          Label contact = new Label(patient.contact);
          contact.getStyleClass().add("list-item-contact");

          HBox h = new HBox(12, left, contact);
          h.setAlignment(Pos.CENTER_LEFT);
          HBox.setHgrow(left, Priority.ALWAYS);

          setText(null);
          setGraphic(h);
        }
      }
    });

    // context menu for quick actions
    patientListView.setCellFactory(orig -> {
      ListCell<Patient> cell = new ListCell<>() {
        @Override
        protected void updateItem(Patient patient, boolean empty) {
          super.updateItem(patient, empty);
          if (empty || patient == null) {
            setText(null);
            setGraphic(null);
          } else {
            Label nameLbl = new Label(patient.name);
            nameLbl.getStyleClass().add("list-item-title");
            Label meta = new Label("Age " + patient.age + " • " + patient.gender);
            meta.getStyleClass().add("list-item-meta");
            VBox left = new VBox(2, nameLbl, meta);
            Label contact = new Label(patient.contact);
            contact.getStyleClass().add("list-item-contact");
            HBox h = new HBox(12, left, contact);
            h.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(left, Priority.ALWAYS);
            setText(null);
            setGraphic(h);
          }
        }
      };

      MenuItem edit = new MenuItem("Edit");
      edit.setOnAction(e -> {
        Patient p = cell.getItem();
        if (p != null) {
          showEditPatientDialog(p);
        }
      });

      MenuItem del = new MenuItem("Delete");
      del.setOnAction(e -> {
        Patient p = cell.getItem();
        if (p != null) {
          try {
            if (apiClient.deletePatient(p.id)) {
              patients.remove(p);
              updateDashboard();
              log("Deleted patient: " + p.name);
            }
          } catch (Exception ex) {
            showError("Could not delete patient: " + friendlyApiError(ex));
          }
        }
      });

      ContextMenu menu = new ContextMenu(edit, del);
      cell.setContextMenu(menu);
      return cell;
    });

    // wire search field to filter
    searchField.textProperty().addListener((obs, oldV, newV) -> {
      String filter = newV == null ? "" : newV.trim().toLowerCase();
      filteredPatients.setPredicate(p -> {
        if (filter.isEmpty())
          return true;
        return p.name.toLowerCase().contains(filter)
            || p.contact.toLowerCase().contains(filter)
            || p.gender.toLowerCase().contains(filter);
      });
    });

    addButton.setOnAction(e -> {
      String name = nameField.getText().trim();
      String ageText = ageField.getText().trim();
      String gender = genderField.getText().trim();
      String contact = contactField.getText().trim();

      if (name.isEmpty() || ageText.isEmpty() || gender.isEmpty() || contact.isEmpty()) {
        showError("All patient fields are required.");
        return;
      }

      int age;
      try {
        age = Integer.parseInt(ageText);
      } catch (NumberFormatException ex) {
        showError("Age must be a valid number.");
        return;
      }

      try {
        Patient patient = apiClient.insertPatient(name, age, gender, contact);
        patients.add(patient);
        nameField.clear();
        ageField.clear();
        genderField.clear();
        contactField.clear();

        refreshPatientSelectors();
        updateDashboard();
        log("Added patient: " + patient);
        if (toastManager != null)
          toastManager.showToast("success", "Added patient: " + patient.name);
      } catch (Exception ex) {
        showError("Could not save patient: " + friendlyApiError(ex));
      }
    });

    Button deleteButton = new Button("Delete Selected");
    deleteButton.setMaxWidth(Double.MAX_VALUE);
    deleteButton.getStyleClass().add("danger-button");
    deleteButton.setOnAction(e -> deleteSelectedPatient());
    deleteButton.setTooltip(new Tooltip("Delete the selected patient"));

    GridPane form = createFormGrid();
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

    VBox leftPane = createCardPane("Add Patient", form);
    leftPane.setPrefWidth(340);

    VBox rightPane = createCardPane("Patient List", patientListView);
    VBox.setVgrow(patientListView, Priority.ALWAYS);

    HBox content = new HBox(16, leftPane, rightPane);
    content.getStyleClass().add("content-split");
    content.setAlignment(Pos.TOP_LEFT);
    HBox.setHgrow(rightPane, Priority.ALWAYS);

    Tab tab = new Tab("Patients", content);
    return tab;
  }

  private Tab createDoctorsTab() {
    TextField nameField = new TextField();
    nameField.setPromptText("Doctor name");

    TextField specializationField = new TextField();
    specializationField.setPromptText("Specialization");

    TextField contactField = new TextField();
    contactField.setPromptText("Contact number");

    Button addButton = new Button("Add Doctor");
    addButton.setMaxWidth(Double.MAX_VALUE);
    addButton.getStyleClass().add("primary-button");

    doctorListView = new ListView<>(doctors);
    doctorListView.setPlaceholder(new Label("No doctors added yet."));
    doctorListView.getStyleClass().add("data-list");
    doctorListView.setCellFactory(list -> new ListCell<>() {
      @Override
      protected void updateItem(Doctor doctor, boolean empty) {
        super.updateItem(doctor, empty);
        if (empty || doctor == null) {
          setText(null);
          setGraphic(null);
        } else {
          Label nameLbl = new Label(doctor.name);
          nameLbl.getStyleClass().add("list-item-title");

          Label meta = new Label(doctor.specialization);
          meta.getStyleClass().add("list-item-meta");

          VBox left = new VBox(2, nameLbl, meta);

          Label contact = new Label(doctor.contact);
          contact.getStyleClass().add("list-item-contact");

          HBox h = new HBox(12, left, contact);
          h.setAlignment(Pos.CENTER_LEFT);
          HBox.setHgrow(left, Priority.ALWAYS);

          setText(null);
          setGraphic(h);
        }
      }
    });

    addButton.setOnAction(e -> {
      String name = nameField.getText().trim();
      String specialization = specializationField.getText().trim();
      String contact = contactField.getText().trim();

      if (name.isEmpty() || specialization.isEmpty() || contact.isEmpty()) {
        showError("All doctor fields are required.");
        return;
      }

      try {
        Doctor doctor = apiClient.insertDoctor(name, specialization, contact);
        doctors.add(doctor);
        nameField.clear();
        specializationField.clear();
        contactField.clear();

        refreshDoctorSelectors();
        updateDashboard();
        log("Added doctor: " + doctor);
        if (toastManager != null)
          toastManager.showToast("success", "Added doctor: " + doctor.name);
      } catch (Exception ex) {
        showError("Could not save doctor: " + friendlyApiError(ex));
      }
    });

    Button deleteButton = new Button("Delete Selected");
    deleteButton.setMaxWidth(Double.MAX_VALUE);
    deleteButton.getStyleClass().add("danger-button");
    deleteButton.setOnAction(e -> deleteSelectedDoctor());
    deleteButton.setTooltip(new Tooltip("Delete the selected doctor"));

    GridPane form = createFormGrid();
    form.addRow(0, new Label("Name"), nameField);
    form.addRow(1, new Label("Specialization"), specializationField);
    form.addRow(2, new Label("Contact"), contactField);
    form.add(addButton, 1, 3);
    form.add(deleteButton, 1, 4);

    ColumnConstraints left = new ColumnConstraints();
    left.setMinWidth(120);
    ColumnConstraints right = new ColumnConstraints();
    right.setHgrow(Priority.ALWAYS);
    form.getColumnConstraints().addAll(left, right);

    VBox leftPane = createCardPane("Add Doctor", form);
    leftPane.setPrefWidth(340);

    VBox rightPane = createCardPane("Doctor List", doctorListView);
    VBox.setVgrow(doctorListView, Priority.ALWAYS);

    HBox content = new HBox(16, leftPane, rightPane);
    content.getStyleClass().add("content-split");
    content.setAlignment(Pos.TOP_LEFT);
    HBox.setHgrow(rightPane, Priority.ALWAYS);

    Tab tab = new Tab("Doctors", content);
    return tab;
  }

  private Tab createAppointmentsTab() {
    patientComboBox = new ComboBox<>(patients);
    patientComboBox.setPromptText("Select patient");
    patientComboBox.setMaxWidth(Double.MAX_VALUE);

    doctorComboBox = new ComboBox<>(doctors);
    doctorComboBox.setPromptText("Select doctor");
    doctorComboBox.setMaxWidth(Double.MAX_VALUE);

    TextField dateField = new TextField();
    dateField.setPromptText("Date and time, e.g. 2026-06-01 10:00");

    Button addButton = new Button("Create Appointment");
    addButton.setMaxWidth(Double.MAX_VALUE);
    addButton.getStyleClass().add("primary-button");
    addButton.setTooltip(new Tooltip("Create an appointment for the selected patient and doctor"));

    appointmentListView = new ListView<>(appointments);
    appointmentListView.setPlaceholder(new Label("No appointments created yet."));
    appointmentListView.getStyleClass().add("data-list");
    appointmentListView.setCellFactory(list -> new ListCell<>() {
      @Override
      protected void updateItem(Appointment appointment, boolean empty) {
        super.updateItem(appointment, empty);
        if (empty || appointment == null) {
          setText(null);
          setGraphic(null);
        } else {
          Patient patient = findPatient(appointment.patientId);
          Doctor doctor = findDoctor(appointment.doctorId);

          Label title = new Label(
              (patient != null ? patient.name : "Unknown") + " — " + (doctor != null ? doctor.name : "Unknown"));
          title.getStyleClass().add("list-item-title");

          Label when = new Label(appointment.scheduledAt);
          when.getStyleClass().add("list-item-meta");

          VBox left = new VBox(2, title, when);

          Label idBadge = new Label("#" + appointment.id);
          idBadge.getStyleClass().add("list-item-badge");

          HBox h = new HBox(12, left, idBadge);
          h.setAlignment(Pos.CENTER_LEFT);
          HBox.setHgrow(left, Priority.ALWAYS);

          setText(null);
          setGraphic(h);
        }
      }
    });

    addButton.setOnAction(e -> {
      Patient patient = patientComboBox.getValue();
      Doctor doctor = doctorComboBox.getValue();
      String date = dateField.getText().trim();

      if (patient == null) {
        showError("Select a patient first.");
        return;
      }
      if (doctor == null) {
        showError("Select a doctor first.");
        return;
      }
      if (date.isEmpty()) {
        showError("Enter date and time.");
        return;
      }

      try {
        Appointment appointment = apiClient.insertAppointment(patient.id, doctor.id, date);
        appointments.add(appointment);
        appointmentListView.refresh();
        dateField.clear();
        updateDashboard();

        log("Created appointment: " + appointment.describe(patient, doctor));
        if (toastManager != null)
          toastManager.showToast("success", "Appointment created: " + appointment.describe(patient, doctor));
      } catch (Exception ex) {
        showError("Could not save appointment: " + friendlyApiError(ex));
      }
    });

    Button deleteButton = new Button("Delete Selected");
    deleteButton.setMaxWidth(Double.MAX_VALUE);
    deleteButton.getStyleClass().add("danger-button");
    deleteButton.setOnAction(e -> deleteSelectedAppointment());
    deleteButton.setTooltip(new Tooltip("Delete the selected appointment"));

    GridPane form = createFormGrid();
    form.addRow(0, new Label("Patient"), patientComboBox);
    form.addRow(1, new Label("Doctor"), doctorComboBox);
    form.addRow(2, new Label("Date/Time"), dateField);
    form.add(addButton, 1, 3);
    form.add(deleteButton, 1, 4);

    ColumnConstraints left = new ColumnConstraints();
    left.setMinWidth(120);
    ColumnConstraints right = new ColumnConstraints();
    right.setHgrow(Priority.ALWAYS);
    form.getColumnConstraints().addAll(left, right);

    VBox leftPane = createCardPane("Create Appointment", form);
    leftPane.setPrefWidth(360);

    VBox rightPane = createCardPane("Appointments", appointmentListView);
    VBox.setVgrow(appointmentListView, Priority.ALWAYS);

    HBox content = new HBox(16, leftPane, rightPane);
    content.getStyleClass().add("content-split");
    content.setAlignment(Pos.TOP_LEFT);
    HBox.setHgrow(rightPane, Priority.ALWAYS);

    Tab tab = new Tab("Appointments", content);
    return tab;
  }

  private Tab createRecordsTab() {
    TextArea summary = new TextArea();
    summary.setEditable(false);
    summary.setWrapText(true);
    summary.getStyleClass().add("summary-area");

    Button refreshButton = new Button("Refresh Summary");
    refreshButton.getStyleClass().add("secondary-button");

    refreshButton.setOnAction(e -> {
      summary.setText(buildSummary());
      log("Summary refreshed.");
    });

    summary.setText(buildSummary());

    VBox box = createCardPane("Records", summary);
    box.getChildren().add(0, refreshButton);
    VBox.setVgrow(summary, Priority.ALWAYS);

    return new Tab("Records", box);
  }

  private VBox createCardPane(String titleText, Region content) {
    Label title = new Label(titleText);
    title.getStyleClass().add("section-title");

    VBox box = new VBox(12, title, content);
    box.setPadding(new Insets(16));
    box.getStyleClass().add("card-pane");
    VBox.setVgrow(content, Priority.ALWAYS);
    return box;
  }

  private VBox createStatCard(String labelText, String valueText) {
    Label label = new Label(labelText);
    label.getStyleClass().add("stat-label");

    Label value = new Label(valueText);
    value.getStyleClass().add("stat-value");

    if ("Patients".equals(labelText)) {
      patientCountLabel = value;
    } else if ("Doctors".equals(labelText)) {
      doctorCountLabel = value;
    } else if ("Appointments".equals(labelText)) {
      appointmentCountLabel = value;
    }

    VBox card = new VBox(4, label, value);
    card.getStyleClass().add("stat-card");
    if ("Patients".equals(labelText)) {
      card.getStyleClass().add("patients-card");
    } else if ("Doctors".equals(labelText)) {
      card.getStyleClass().add("doctors-card");
    } else if ("Appointments".equals(labelText)) {
      card.getStyleClass().add("appointments-card");
    }
    card.setPrefWidth(150);
    return card;
  }

  private void updateDashboard() {
    if (patientCountLabel != null) {
      patientCountLabel.setText(String.valueOf(patients.size()));
    }
    if (doctorCountLabel != null) {
      doctorCountLabel.setText(String.valueOf(doctors.size()));
    }
    if (appointmentCountLabel != null) {
      appointmentCountLabel.setText(String.valueOf(appointments.size()));
    }
  }

  private GridPane createFormGrid() {
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.getStyleClass().add("form-grid");
    return grid;
  }

  private void playIntroAnimation(Node topSection, Node tabPane, Node bottomBox) {
    animateEntrance(topSection, 0.0);
    animateEntrance(tabPane, 0.10);
    animateEntrance(bottomBox, 0.18);
  }

  private void animateEntrance(Node node, double delaySeconds) {
    node.setOpacity(0);
    node.setTranslateY(18);

    FadeTransition fade = new FadeTransition(Duration.millis(420), node);
    fade.setFromValue(0);
    fade.setToValue(1);

    TranslateTransition slide = new TranslateTransition(Duration.millis(460), node);
    slide.setFromY(18);
    slide.setToY(0);

    ParallelTransition transition = new ParallelTransition(fade, slide);
    transition.setDelay(Duration.seconds(delaySeconds));
    transition.play();
  }

  private void refreshPatientSelectors() {
    if (patientComboBox != null) {
      patientComboBox.setItems(patients);
      patientComboBox.setPromptText("Select patient");
    }
  }

  private void refreshDoctorSelectors() {
    if (doctorComboBox != null) {
      doctorComboBox.setItems(doctors);
      doctorComboBox.setPromptText("Select doctor");
    }
  }

  private void log(String message) {
    if (statusArea != null) {
      statusArea.appendText(message + System.lineSeparator());
    }
    if (dashboardView != null)
      dashboardView.addActivity(message);
  }

  private void showError(String message) {
    // Ensure dialogs are shown on the FX Application Thread to avoid
    // animation/layout timing issues
    Platform.runLater(() -> {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Input Error");
      alert.setHeaderText(null);
      alert.setContentText(message);
      alert.showAndWait();
    });
  }

  private void loadDataFromMongo(boolean showPopupOnFailure) {
    // Run DB access in a background thread and update UI on the FX thread.
    new Thread(() -> {
      try {
        List<Patient> p = apiClient.loadPatients();
        List<Doctor> d = apiClient.loadDoctors();
        List<Appointment> a = apiClient.loadAppointments();
        apiUnavailableNotified = false;

        javafx.application.Platform.runLater(() -> {
          patients.setAll(p);
          doctors.setAll(d);
          appointments.setAll(a);

          // UI updates
          refreshPatientSelectors();
          refreshDoctorSelectors();
          if (appointmentListView != null)
            appointmentListView.refresh();
          updateDashboard();
        });
      } catch (Exception exception) {
        String friendly = friendlyApiError(exception);
        log("Mongo refresh failed: " + friendly);
        if (showPopupOnFailure) {
          javafx.application.Platform.runLater(() -> showError("Failed to load data from MongoDB: " + friendly));
        } else if (!apiUnavailableNotified) {
          apiUnavailableNotified = true;
          javafx.application.Platform.runLater(() -> showError("MongoDB not reachable. " + friendly));
        }
      }
    }, "mongo-refresh-thread").start();
  }

  private void startAutoRefresh() {
    autoRefreshTimeline = new Timeline(
        new KeyFrame(Duration.seconds(AUTO_REFRESH_SECONDS), e -> loadDataFromMongo(false)));
    autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
    autoRefreshTimeline.play();
  }

  private String friendlyApiError(Exception exception) {
    Throwable cause = exception;
    while (cause.getCause() != null) {
      cause = cause.getCause();
    }

    String message = cause.getMessage() != null ? cause.getMessage() : exception.getMessage();
    if (message != null && (message.contains("Connection refused") || message.contains("Failed to connect")
        || message.contains("Timed out") || message.contains("server selection"))) {
      return "Cannot reach MongoDB at " + maskMongoUri(apiClient.getBaseUrl())
          + ". Start MongoDB, then try again with -Dhospital.mongo.uri=...";
    }

    return message != null ? message : "Unexpected API error";
  }

  private void deleteSelectedPatient() {
    Patient selected = patientListView != null ? patientListView.getSelectionModel().getSelectedItem() : null;
    if (selected == null) {
      showError("Select a patient to delete.");
      return;
    }

    try {
      if (apiClient.deletePatient(selected.id)) {
        patients.remove(selected);
        appointments.removeIf(a -> a.patientId == selected.id);
        refreshPatientSelectors();
        updateDashboard();
        if (appointmentListView != null) {
          appointmentListView.refresh();
        }
        log("Deleted patient: " + selected.name);
        if (toastManager != null)
          toastManager.showToast("success", "Deleted patient: " + selected.name);
      }
    } catch (Exception exception) {
      showError("Could not delete patient: " + friendlyApiError(exception));
    }
  }

  private void showEditPatientDialog(Patient patient) {
    if (patient == null)
      return;

    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Edit Patient");

    ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

    GridPane grid = createFormGrid();
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

    dialog.showAndWait().ifPresent(result -> {
      try {
        int age = Integer.parseInt(ageField.getText().trim());
        String name = nameField.getText().trim();
        String gender = genderField.getText().trim();
        String contact = contactField.getText().trim();
        if (name.isEmpty() || gender.isEmpty() || contact.isEmpty()) {
          showError("All fields required.");
          return;
        }

        boolean ok = apiClient.updatePatient(patient.id, name, age, gender, contact);
        if (ok) {
          // update local list
          patient.name = name;
          patient.age = age;
          patient.gender = gender;
          patient.contact = contact;
          if (patientListView != null)
            patientListView.refresh();
          refreshPatientSelectors();
          updateDashboard();
          log("Updated patient: " + patient.name);
          if (toastManager != null)
            toastManager.showToast("success", "Updated patient: " + patient.name);
        } else {
          showError("Update failed: No change saved.");
        }
      } catch (NumberFormatException ex) {
        showError("Age must be a number.");
      } catch (Exception ex) {
        showError("Could not update patient: " + friendlyApiError(ex));
      }
    });
  }

  private void deleteSelectedDoctor() {
    Doctor selected = doctorListView != null ? doctorListView.getSelectionModel().getSelectedItem() : null;
    if (selected == null) {
      showError("Select a doctor to delete.");
      return;
    }

    try {
      if (apiClient.deleteDoctor(selected.id)) {
        doctors.remove(selected);
        appointments.removeIf(a -> a.doctorId == selected.id);
        refreshDoctorSelectors();
        updateDashboard();
        if (appointmentListView != null) {
          appointmentListView.refresh();
        }
        log("Deleted doctor: " + selected.name);
        if (toastManager != null)
          toastManager.showToast("success", "Deleted doctor: " + selected.name);
      }
    } catch (Exception exception) {
      showError("Could not delete doctor: " + friendlyApiError(exception));
    }
  }

  private void deleteSelectedAppointment() {
    Appointment selected = appointmentListView != null ? appointmentListView.getSelectionModel().getSelectedItem()
        : null;
    if (selected == null) {
      showError("Select an appointment to delete.");
      return;
    }

    try {
      if (apiClient.deleteAppointment(selected.id)) {
        appointments.remove(selected);
        updateDashboard();
        log("Deleted appointment ID: " + selected.id);
        if (toastManager != null)
          toastManager.showToast("success", "Deleted appointment: #" + selected.id);
      }
    } catch (Exception exception) {
      showError("Could not delete appointment: " + friendlyApiError(exception));
    }
  }

  private String buildSummary() {
    StringBuilder sb = new StringBuilder();
    sb.append("Patients: ").append(patients.size()).append("\n");
    sb.append("Doctors: ").append(doctors.size()).append("\n");
    sb.append("Appointments: ").append(appointments.size()).append("\n\n");

    sb.append("Patient Records:\n");
    if (patients.isEmpty()) {
      sb.append("  None\n");
    } else {
      for (Patient p : patients) {
        sb.append("  ").append(p).append("\n");
      }
    }

    sb.append("\nDoctor Records:\n");
    if (doctors.isEmpty()) {
      sb.append("  None\n");
    } else {
      for (Doctor d : doctors) {
        sb.append("  ").append(d).append("\n");
      }
    }

    sb.append("\nAppointments:\n");
    if (appointments.isEmpty()) {
      sb.append("  None\n");
    } else {
      for (Appointment a : appointments) {
        Patient p = findPatient(a.patientId);
        Doctor d = findDoctor(a.doctorId);
        sb.append("  ").append(a.describe(p, d)).append("\n");
      }
    }

    return sb.toString();
  }

  private Patient findPatient(int id) {
    for (Patient patient : patients) {
      if (patient.id == id) {
        return patient;
      }
    }
    return null;
  }

  private Doctor findDoctor(int id) {
    for (Doctor doctor : doctors) {
      if (doctor.id == id) {
        return doctor;
      }
    }
    return null;
  }

  @Override
  public void stop() {
    if (autoRefreshTimeline != null) {
      autoRefreshTimeline.stop();
    }
    log("Application closed.");
  }

  public static void main(String[] args) {
    launch(args);
  }

  static class Patient {
    int id;
    String name;
    int age;
    String gender;
    String contact;

    Patient(int id, String name, int age, String gender, String contact) {
      this.id = id;
      this.name = name;
      this.age = age;
      this.gender = gender;
      this.contact = contact;
    }

    @Override
    public String toString() {
      return "ID: " + id + " | " + name + " | Age: " + age + " | " + gender + " | " + contact;
    }
  }

  static class Doctor {
    int id;
    String name;
    String specialization;
    String contact;

    Doctor(int id, String name, String specialization, String contact) {
      this.id = id;
      this.name = name;
      this.specialization = specialization;
      this.contact = contact;
    }

    @Override
    public String toString() {
      return "ID: " + id + " | " + name + " | " + specialization + " | " + contact;
    }
  }

  static class Appointment {
    int id;
    int patientId;
    int doctorId;
    String scheduledAt;

    Appointment(int id, int patientId, int doctorId, String scheduledAt) {
      this.id = id;
      this.patientId = patientId;
      this.doctorId = doctorId;
      this.scheduledAt = scheduledAt;
    }

    String describe(Patient patient, Doctor doctor) {
      String patientName = patient != null ? patient.name : "Unknown";
      String doctorName = doctor != null ? doctor.name : "Unknown";
      return "ID: " + id + " | Patient: " + patientName + " | Doctor: " + doctorName + " | When: " + scheduledAt;
    }

    @Override
    public String toString() {
      return "Appointment ID: " + id + " | Patient ID: " + patientId + " | Doctor ID: " + doctorId + " | "
          + scheduledAt;
    }
  }

}
