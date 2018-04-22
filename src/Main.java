import NoiseGenerator.NoiseGenerator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Stage stage=new Stage();
        FXMLLoader fxmlLoader=new FXMLLoader(getClass().getResource("NoiseGenerator/NoiseGenerator.fxml"));
        Parent root = fxmlLoader.load();
        NoiseGenerator noiseGenerator=fxmlLoader.getController();
        Scene scene=new Scene(root, 535, 535, Color.BLACK);
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(we -> noiseGenerator.formClosed());
    }

    public static void main(String[] args) {
        launch(args);
    }
}