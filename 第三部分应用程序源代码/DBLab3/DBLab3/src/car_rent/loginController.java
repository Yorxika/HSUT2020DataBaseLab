package car_rent;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.sql.ResultSet;

import static java.lang.System.exit;

public class loginController {

    @FXML
    private Button loginButton;
    @FXML
    private Button exitButton;
    @FXML
    private TextField inputUsername;
    @FXML
    private PasswordField inputPassword;
    @FXML
    public ChoiceBox<String> loginType;
    @FXML
    public Label labelStatus;
    String loginIdenty = "顾客";

    @FXML
    private void initialize(){
        loginButton.setOnKeyReleased(keyEvent -> {
            if(keyEvent.getCode() == KeyCode.ENTER)
                onLoginClick();
        });
        exitButton.setOnKeyReleased(keyEvent -> {
            if(keyEvent.getCode() == KeyCode.ENTER)
                //onExitClick();
                exit(0);
        });
        inputPassword.setOnKeyReleased(keyEvent -> {
            if(keyEvent.getCode() == KeyCode.ENTER)
                onLoginClick();
        });

    }

    @FXML
    private void onExitClick(){
        //Event.fireEvent(main.getInitStage(),new WindowEvent(main.getInitStage(),WindowEvent.WINDOW_CLOSE_REQUEST));
        exit(0);
    }

    @FXML
    private void onLoginClick(){
        if(!checkInputValid())
            return;
        if(loginType.getValue().equals("员工"))
            loginIdenty = "员工";
        try{
            if(tryLogin()) {
                String nextScene = "customer.fxml";
                String resource = "customer.css";
                String title = "租车系统";
                Stage nextStage = (Stage) loginButton.getScene().getWindow();
                nextStage.close();
                if (loginIdenty.equals("员工")) {
                    employeeController.setUserId(inputUsername.getText().trim());
                    ResultSet resultSet = DBConnector.getInstance().getEmployeeInfo(inputUsername.getText().trim());
                    while (resultSet.next()){
                        employeeController.setUserName(resultSet.getString("name"));
                        employeeController.setTitle(resultSet.getInt("title"));
                    }
                    nextScene = "employee.fxml";
                    resource = "employee.css";
                } else {
                    customerController.setUserId(inputUsername.getText().trim());
                    ResultSet resultSet = DBConnector.getInstance().getCustomerInfo(inputUsername.getText().trim());
                    while (resultSet.next()){
                        customerController.setUserName(resultSet.getString("name"));
                    }
                }
                Parent root = FXMLLoader.load(getClass().getResource(nextScene));
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource(resource).toExternalForm());
                scene.setRoot(root);
                Stage stage = new Stage();
                stage.setTitle(title);
                stage.setScene(scene);
                stage.show();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 检查输入是否有效
     * @return 检查登录是否有效
     */
    private boolean checkInputValid(){
        if(!inputUsername.getText().isEmpty())
            inputUsername.setStyle("-fx-background-color: white;");
        if(!inputPassword.getText().isEmpty())
            inputPassword.setStyle("-fx-background-color: white;");
        if (inputUsername.getText().isEmpty()) {
            inputUsername.setStyle("-fx-background-color: pink;");
            labelStatus.setText("请输入用户名");
            labelStatus.setStyle("-fx-text-fill: red;");
            return false;
        }
        if (inputPassword.getText().isEmpty()) {
            inputPassword.setStyle("-fx-background-color: pink;");
            labelStatus.setText("请输入密码");
            labelStatus.setStyle("-fx-text-fill: red;");
            return false;
        }

        labelStatus.setText("登录中...");
        labelStatus.setStyle("");
        return true;
    }

    /**
     * 尝试登录
     * @return 登录成功与否
     */
    public boolean tryLogin(){
        if(!checkInputValid())
            return false;
        ResultSet result = loginIdenty.equals("顾客") ?
                DBConnector.getInstance().getCustomerInfo(inputUsername.getText().trim()):
                DBConnector.getInstance().getEmployeeInfo(inputUsername.getText().trim());
        if(result == null){
            labelStatus.setText("数据库读取错误");
            labelStatus.setStyle("-fx-text-fill: red;");
        }
        try {
            if(!result.next()){
                labelStatus.setText("用户不存在");
                inputUsername.setStyle("-fx-background-color: pink;");
                labelStatus.setStyle("-fx-text-fill: red;");
                return false;
            }else if(!((ResultSet) result).getString("passwd").equals(inputPassword.getText())) {
                labelStatus.setText("密码错误");
                labelStatus.setStyle("-fx-text-fill: red;");
                inputPassword.setStyle("-fx-background-color: pink;");
                return false;
            }
            //登录成功需要设置最后一次登录事件
            DBConnector.getInstance().updateDLRQ(loginIdenty,inputUsername.getText().trim());
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
