import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class AppointmentsView extends Tab {
  private final ApiClient apiClient;
  private final ObservableList<HospitalManagementFX.Patient> patients;
  private final ObservableList<HospitalManagementFX.Doctor> doctors;
  private final ObservableList<HospitalManagementFX.Appointment> appointments;
  private final ToastManager toastManager;

  private ListView<HospitalManagementFX.Appointment> appointmentListView;

  public AppointmentsView(ApiClient apiClient,
                          ObservableList<HospitalManagementFX.Patient> patients,
                          ObservableList<HospitalManagementFX.Doctor> doctors,
                          ObservableList<HospitalManagementFX.Appointment> appointments,
                          ToastManager toastManager) {
    super("Appointments");
    this.apiClient = apiClient;
    this.patients = patients;
    this.doctors = doctors;
    this.appointments = appointments;
    this.toastManager = toastManager;

    setContent(buildContent());
  }

  private Node buildContent() {
    ComboBox<HospitalManagementFX.Patient> patientCombo = new ComboBox<>(patients);
    patientCombo.setPromptText("Select patient");
    patientCombo.setMaxWidth(Double.MAX_VALUE);

    ComboBox<HospitalManagementFX.Doctor> doctorCombo = new ComboBox<>(doctors);
    doctorCombo.setPromptText("Select doctor");

    TextField dateField = new TextField(); dateField.setPromptText("Date and time, e.g. 2026-06-01 10:00");
    Button addBtn = new Button("Create Appointment"); addBtn.getStyleClass().addAll("primary-button","primary"); addBtn.setMaxWidth(Double.MAX_VALUE);
    Button deleteBtn = new Button("Delete Selected"); deleteBtn.getStyleClass().add("danger-button"); deleteBtn.setMaxWidth(Double.MAX_VALUE);

    GridPane form = new GridPane(); form.setHgap(10); form.setVgap(10);
    form.addRow(0, new Label("Patient"), patientCombo); form.addRow(1, new Label("Doctor"), doctorCombo); form.addRow(2, new Label("Date/Time"), dateField);
    form.add(addBtn, 1, 3); form.add(deleteBtn, 1, 4);
    ColumnConstraints left = new ColumnConstraints(); left.setMinWidth(120); ColumnConstraints right = new ColumnConstraints(); right.setHgrow(Priority.ALWAYS); form.getColumnConstraints().addAll(left, right);

    VBox leftPane = new VBox(12, new Label("Create Appointment"), form); leftPane.getStyleClass().add("card-pane"); leftPane.setPrefWidth(360);

    FilteredList<HospitalManagementFX.Appointment> filtered = new FilteredList<>(appointments, a->true);
    appointmentListView = new ListView<>(filtered); appointmentListView.setPlaceholder(new Label("No appointments yet")); appointmentListView.getStyleClass().add("data-list");

    appointmentListView.setCellFactory(list -> new ListCell<>(){
      @Override
      protected void updateItem(HospitalManagementFX.Appointment a, boolean empty) {
        super.updateItem(a, empty);
        if (empty || a == null) { setText(null); setGraphic(null); }
        else {
          HospitalManagementFX.Patient p = findPatient(a.patientId);
          HospitalManagementFX.Doctor d = findDoctor(a.doctorId);
          Label title = new Label((p!=null? p.name: "Unknown") + " — " + (d!=null? d.name: "Unknown")); title.getStyleClass().add("list-item-title");
          Label when = new Label(a.scheduledAt); when.getStyleClass().add("list-item-meta");
          Label badge = new Label("#"+a.id); badge.getStyleClass().add("list-item-badge");
          VBox v = new VBox(2, title, when);
          HBox h = new HBox(12, v, badge); HBox.setHgrow(v, Priority.ALWAYS);
          setGraphic(h);
        }
      }
    });

    VBox rightPane = new VBox(6, new Label("Appointments"), appointmentListView); rightPane.getStyleClass().add("card-pane"); VBox.setVgrow(appointmentListView, Priority.ALWAYS);

    HBox content = new HBox(16, leftPane, rightPane); content.setAlignment(Pos.TOP_LEFT);
    VBox container = new VBox(10, content); container.setPadding(new Insets(8));

    addBtn.setOnAction(e -> {
      HospitalManagementFX.Patient patient = patientCombo.getValue(); HospitalManagementFX.Doctor doctor = doctorCombo.getValue(); String date = dateField.getText().trim();
      if (patient==null || doctor==null || date.isEmpty()) { new Alert(Alert.AlertType.ERROR, "Select patient, doctor and enter date").showAndWait(); return; }
      try { HospitalManagementFX.Appointment ap = apiClient.insertAppointment(patient.id, doctor.id, date); appointments.add(ap); appointmentListView.refresh(); dateField.clear(); if (toastManager!=null) toastManager.showToast("success","Appointment created: #"+ap.id); } catch (Exception ex) { new Alert(Alert.AlertType.ERROR, "Could not create appointment: "+ex.getMessage()).showAndWait(); }
    });

    deleteBtn.setOnAction(e -> { HospitalManagementFX.Appointment sel = appointmentListView.getSelectionModel().getSelectedItem(); if (sel==null) { new Alert(Alert.AlertType.ERROR,"Select appointment to delete").showAndWait(); return;} try { if (apiClient.deleteAppointment(sel.id)) { appointments.remove(sel); if (toastManager!=null) toastManager.showToast("success","Deleted appointment: #"+sel.id); } } catch (Exception ex) { new Alert(Alert.AlertType.ERROR, "Could not delete appointment: "+ex.getMessage()).showAndWait(); } });

    return container;
  }

  private HospitalManagementFX.Patient findPatient(int id) { for (HospitalManagementFX.Patient p: patients) if (p.id==id) return p; return null; }
  private HospitalManagementFX.Doctor findDoctor(int id) { for (HospitalManagementFX.Doctor d: doctors) if (d.id==id) return d; return null; }
}

      
      
      
      
    
     
    
    
    
    
    
    
    
    
    
    
    
    
    
      
    
     
          
          
                 
          
            
          
          
    
    
    
    
      
          
        
        
      
        
        
        
        
          
             
      
          
      
      
        
         
        
      
      
        
          
            
               
        
      
          
      
    
     
        
        
    
  

  
     
        
        
    
  