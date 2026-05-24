package com.example.sqlitefx.ui;

import com.example.sqlitefx.dao.UserDao;
import com.example.sqlitefx.db.Database;
import com.example.sqlitefx.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;

public class UserCrudView extends BorderPane {

  private final UserDao userDao;
  private final ObservableList<User> users = FXCollections.observableArrayList();

  private final TableView<User> tableView = new TableView<>();
  private final TextField fullNameField = new TextField();
  private final TextField emailField = new TextField();
  private final TextField phoneField = new TextField();
  private final Label statusLabel = new Label();
  private final Button addButton = new Button("Add");
  private final Button updateButton = new Button("Update");
  private final Button deleteButton = new Button("Delete");
  private final Button clearButton = new Button("Clear");

  public UserCrudView() {
    try {
      userDao = new UserDao();
    } catch (RuntimeException exception) {
      showError("Database startup failed",
          exception.getCause() != null ? exception.getCause().getMessage() : exception.getMessage());
      throw exception;
    }

    setPadding(new Insets(16));
    setTop(createHeader());
    setLeft(createFormPane());
    setCenter(createTablePane());
    setBottom(createStatusBar());

    configureTable();
    configureActions();
    loadUsers();
  }

  private VBox createHeader() {
    Label title = new Label("JavaFX + SQLite CRUD Demo");
    title.getStyleClass().add("app-title");

    Label subtitle = new Label("Create, read, update, and delete users with JDBC and SQLite.");
    subtitle.getStyleClass().add("app-subtitle");

    VBox header = new VBox(4, title, subtitle);
    header.setPadding(new Insets(0, 0, 16, 0));
    return header;
  }

  private VBox createFormPane() {
    fullNameField.setPromptText("Full name");
    emailField.setPromptText("Email");
    phoneField.setPromptText("Phone");

    GridPane form = new GridPane();
    form.setHgap(10);
    form.setVgap(10);

    form.addRow(0, new Label("Full name"), fullNameField);
    form.addRow(1, new Label("Email"), emailField);
    form.addRow(2, new Label("Phone"), phoneField);

    GridPane.setHgrow(fullNameField, Priority.ALWAYS);
    GridPane.setHgrow(emailField, Priority.ALWAYS);
    GridPane.setHgrow(phoneField, Priority.ALWAYS);

    addButton.getStyleClass().add("primary-button");
    updateButton.getStyleClass().add("secondary-button");
    deleteButton.getStyleClass().add("danger-button");
    clearButton.getStyleClass().add("secondary-button");

    HBox buttonRow = new HBox(10, addButton, updateButton, deleteButton, clearButton);
    buttonRow.setAlignment(Pos.CENTER_LEFT);

    VBox panel = new VBox(14,
        new Label("User Form"),
        form,
        buttonRow,
        new Label("Tip: click a table row to edit it."));
    panel.setPadding(new Insets(0, 16, 0, 0));
    panel.setPrefWidth(360);
    panel.getStyleClass().add("card");
    return panel;
  }

  private VBox createTablePane() {
    tableView.setItems(users);
    tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    tableView.setPlaceholder(new Label("No users yet. Add one from the form."));

    VBox tablePanel = new VBox(10, new Label("Users"), tableView);
    tablePanel.getStyleClass().add("card");
    VBox.setVgrow(tableView, Priority.ALWAYS);
    return tablePanel;
  }

  private HBox createStatusBar() {
    statusLabel.getStyleClass().add("status-label");
    HBox statusBar = new HBox(statusLabel);
    statusBar.setPadding(new Insets(14, 0, 0, 0));
    return statusBar;
  }

  private void configureTable() {
    TableColumn<User, Integer> idColumn = new TableColumn<>("ID");
    idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

    TableColumn<User, String> nameColumn = new TableColumn<>("Full Name");
    nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));

    TableColumn<User, String> emailColumn = new TableColumn<>("Email");
    emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

    TableColumn<User, String> phoneColumn = new TableColumn<>("Phone");
    phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));

    TableColumn<User, String> createdColumn = new TableColumn<>("Created At");
    createdColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

    tableView.getColumns().setAll(idColumn, nameColumn, emailColumn, phoneColumn, createdColumn);
    tableView.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, selectedUser) -> populateForm(selectedUser));
  }

  private void configureActions() {
    addButton.setOnAction(event -> handleAdd());
    updateButton.setOnAction(event -> handleUpdate());
    deleteButton.setOnAction(event -> handleDelete());
    clearButton.setOnAction(event -> clearForm());
  }

  private void handleAdd() {
    String validationError = validateForm(false);
    if (validationError != null) {
      showError("Validation error", validationError);
      return;
    }

    User user = new User(
        fullNameField.getText().trim(),
        emailField.getText().trim(),
        phoneField.getText().trim());

    try {
      userDao.insert(user);
      loadUsers();
      clearForm();
      showStatus("User added successfully. Database file: " + Database.getDatabaseFile().toAbsolutePath());
    } catch (SQLException exception) {
      showError("Insert failed", exception.getMessage());
    }
  }

  private void handleUpdate() {
    User selectedUser = tableView.getSelectionModel().getSelectedItem();
    if (selectedUser == null) {
      showError("Selection required", "Select a user row first, then click Update.");
      return;
    }

    String validationError = validateForm(true);
    if (validationError != null) {
      showError("Validation error", validationError);
      return;
    }

    selectedUser.setFullName(fullNameField.getText().trim());
    selectedUser.setEmail(emailField.getText().trim());
    selectedUser.setPhone(phoneField.getText().trim());

    try {
      if (userDao.update(selectedUser)) {
        loadUsers();
        showStatus("User updated successfully.");
      } else {
        showError("Update failed", "No row was updated.");
      }
    } catch (SQLException exception) {
      showError("Update failed", exception.getMessage());
    }
  }

  private void handleDelete() {
    User selectedUser = tableView.getSelectionModel().getSelectedItem();
    if (selectedUser == null) {
      showError("Selection required", "Select a user row first, then click Delete.");
      return;
    }

    try {
      if (userDao.delete(selectedUser.getId())) {
        loadUsers();
        clearForm();
        showStatus("User deleted successfully.");
      } else {
        showError("Delete failed", "No row was deleted.");
      }
    } catch (SQLException exception) {
      showError("Delete failed", exception.getMessage());
    }
  }

  private void loadUsers() {
    try {
      List<User> currentUsers = userDao.findAll();
      users.setAll(currentUsers);
      showStatus(
          "Loaded " + currentUsers.size() + " user(s). SQLite file: " + Database.getDatabaseFile().toAbsolutePath());
    } catch (SQLException exception) {
      showError("Load failed", exception.getMessage());
    }
  }

  private String validateForm(boolean requireSelection) {
    if (requireSelection && tableView.getSelectionModel().getSelectedItem() == null) {
      return "Select a user row first.";
    }

    if (fullNameField.getText().trim().isEmpty()) {
      return "Full name is required.";
    }

    if (emailField.getText().trim().isEmpty()) {
      return "Email is required.";
    }

    return null;
  }

  private void populateForm(User user) {
    if (user == null) {
      return;
    }

    fullNameField.setText(user.getFullName());
    emailField.setText(user.getEmail());
    phoneField.setText(user.getPhone());
  }

  private void clearForm() {
    tableView.getSelectionModel().clearSelection();
    fullNameField.clear();
    emailField.clear();
    phoneField.clear();
  }

  private void showStatus(String message) {
    statusLabel.setText(message);
  }

  private void showError(String title, String message) {
    statusLabel.setText(title + ": " + message);
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
