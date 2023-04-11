package client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import server.models.Course;

public class ClientController {
    @FXML
    private TableView<Course> coursesTable;
    @FXML
    private TableColumn<Course, String> courseCodeColumn;
    @FXML
    private TableColumn<Course, String> courseNameColumn;
    @FXML
    private ComboBox<String> semesterComboBox;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField studentIdField;

    @FXML
    public void initialize() {
        courseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    }

    @FXML
    protected void loadCourses(ActionEvent event) {
        // Send "CHARGER" request to the server and update the table.
        // For demonstration purposes, let's create a sample list of courses
        ObservableList<Course> courses = FXCollections.observableArrayList(
            new Course("Introduction to Computer Science", "COMP-101", "Automne"),
            new Course("Data Structures and Algorithms", "COMP-102", "Hiver")
        );
        coursesTable.setItems(courses);
    }

    @FXML
    protected void registerCourse(ActionEvent event) {
        // Check if a course is selected
        Course selectedCourse = coursesTable.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) {
            showError("Erreur: Veuillez sélectionner un cours");
            return;
        }

        // Check if all text fields are filled
        if (firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty() ||
            emailField.getText().isEmpty() || studentIdField.getText().isEmpty()) {
            showError("Erreur: Tous les champs doivent être remplis");
            return;
        }

        // Check for valid email
        String email = emailField.getText();
        if (!email.contains("@") || (!email.endsWith(".com") && !email.endsWith(".ca"))) {
            showError("Erreur: addresse email invalide");
            return;
        }

        // Send "INSCRIRE" request to the server
        System.out.println("Inscription réussie!");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
