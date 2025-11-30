import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import controller.MainController;
import service.EmailService;
import service.FolderManager;
import service.HostConfigManager;

/**
 * Main application entry point
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load main window FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/MainWindow.fxml"));
        Parent root = loader.load();

        // Inject services into controller
        MainController controller = loader.getController();
        EmailService emailService = new EmailService();
        FolderManager folderManager = new FolderManager();
        HostConfigManager hostConfigManager = new HostConfigManager();

        controller.setEmailService(emailService);
        controller.setFolderManager(folderManager);
        // Pass hostConfigManager if needed for switching hosts

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
