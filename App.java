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

        // Create services
        EmailService emailService = new EmailService();
        FolderManager folderManager = new FolderManager();
        HostConfigManager hostConfigManager = new HostConfigManager();


        try {
            hostConfigManager.loadFromFile("host_config.dat");
            if (hostConfigManager.getCurrentHost() != null) {
                emailService.setHostConfiguration(hostConfigManager.getCurrentHost());
            }
        } catch (Exception ex) {
            // Ignore if file not found / corrupted, user can configure manually
            ex.printStackTrace();
        }

        // Inject services into controller
        MainController controller = loader.getController();
        controller.setEmailService(emailService);
        controller.setFolderManager(folderManager);
        controller.setHostConfigManager(hostConfigManager);

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

