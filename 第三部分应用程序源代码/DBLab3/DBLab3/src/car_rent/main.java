package car_rent;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import static java.lang.System.exit;

public class main extends Application {

    private static Stage initStage = null;

    @Override
    public void start(Stage stage) throws Exception {
        initStage = stage;
        DBConnector.getInstance().connectDB("localhost",3306,"dblab3","root","root");
        stage.setTitle("汽车租借系统");
        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
        Scene scene = new Scene(root, 598, 343);
        scene.getStylesheets().add(getClass().getResource("login.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    public static Stage getInitStage() {
        return initStage;
    }

    public static void clickedExitButton(){
        exit(0);
    }

}
