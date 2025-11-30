import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application entry point
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load main window FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/MainWindow.fxml"));
        Parent root = loader.load();

        // Setup scene
        Scene scene = new Scene(root, 1024, 768);

        // Setup stage
        primaryStage.setTitle("Email Client");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
