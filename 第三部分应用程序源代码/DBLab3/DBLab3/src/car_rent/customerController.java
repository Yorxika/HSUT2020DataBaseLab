package car_rent;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.lang.System.exit;

public class customerController {

    /**
     * 租车信息内部类
     */
    static class rentCarInfo{
        public StringProperty license;
        public StringProperty brand;
        public StringProperty status;
        public StringProperty cost;
        public StringProperty ya;
        public StringProperty endTime;

        public rentCarInfo(String license, String brand, int status, int cost, int ya, String endTime){
            this.license = new SimpleStringProperty(license);
            this.brand = new SimpleStringProperty(brand);
            this.status = new SimpleStringProperty(DBConnector.carStatus.getMsg(status));
            this.cost = new SimpleStringProperty("" + cost);
            this.ya = new SimpleStringProperty("" + ya);
            this.endTime = new SimpleStringProperty(endTime);
        }
    }

    @FXML
    private Button confirmButton;
    @FXML
    private Button clearButton;
    @FXML
    private Button logoutButton;
    @FXML
    private Button exitButton;
    @FXML
    private Button returnCarButton;
    @FXML
    private Button logoutButton_2;
    @FXML
    private Button exitButton_2;

    @FXML
    private ComboBox<String> carBrand;
    @FXML
    private ComboBox<String> carId;
    @FXML
    private ComboBox<String> carPay;
    @FXML
    private ComboBox<String> carStatus;
    @FXML
    private ComboBox<String> carName;  //经手人

    @FXML
    private DatePicker startDate;
    @FXML
    private DatePicker endDate;

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private TextField carCost;

    @FXML
    private Tab rentTab;
    @FXML
    private Tab returnTab;

    @FXML
    private TableView<rentCarInfo> rentInfo;
    @FXML
    private TableColumn<rentCarInfo,String> carId_r;
    @FXML
    private TableColumn<rentCarInfo,String> carBrand_r;
    @FXML
    private TableColumn<rentCarInfo,String> carStatus_r;
    @FXML
    private TableColumn<rentCarInfo,String> carCost_r;
    @FXML
    private TableColumn<rentCarInfo,String> carYa_r;
    @FXML
    private TableColumn<rentCarInfo,String> carEndDate;


    private static String userName;  //顾客姓名
    private static String userId;  //顾客ID

    private String brand;
    private String license;
    private String pay;
    private String status;
    private String empId;


    private int lastCarBrand = -1;
    private int lastCarPay = -1;
    private int lastCarStatus = -1;
    private int lastCarId = -1;
    private int lastCarName = -1;

    private ObservableList<rentCarInfo> rentCarInfoList = FXCollections.observableArrayList();

    @FXML
    private void initialize(){
        welcomeLabel.setText("欢迎登录租车系统, " + userName + "!");
        statusLabel.setText("");
        startDate.setValue(LocalDate.now());
        endDate.setValue(LocalDate.now());
        carName.getItems().addAll(DBConnector.getInstance().getEmployeeInfo());
        carBrand.addEventHandler(ComboBox.ON_SHOWING,event -> {
            if(!carBrand.getEditor().getText().isEmpty())
                return;
            setCarBrand();
            event.consume();
        });
        carBrand.getEditor().setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER || keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.DOWN)
                return;
            setCarBrand();
            if(!carBrand.isShowing())
                carBrand.show();
            else{
                carBrand.hide();
                carBrand.show();
            }
        });
        carBrand.addEventHandler(ComboBox.ON_HIDDEN,event -> {
            int index = carBrand.getSelectionModel().getSelectedIndex();
            if(index != lastCarBrand && index != -1){
                lastCarBrand = index;
                brand = carBrand.getItems().get(lastCarBrand);
                lastCarId = -1;
                carId.getItems().clear();
                carPay.getItems().clear();
                carPay.setDisable(false);
                carStatus.setDisable(false);
                carPay.setPromptText("");
                carStatus.setPromptText("");
                carStatus.getItems().clear();
                carCost.setText("");
            }
            statusLabel.setText("");
        });
        carPay.addEventHandler(ComboBox.ON_SHOWING,event -> {
            if(carStatus.getSelectionModel().getSelectedIndex() == -1){
                setCarPay(null);
                return;
            }
            String status = carStatus.getSelectionModel().getSelectedItem();
            if(status.isEmpty())
                status = null;
            status = "" + DBConnector.carStatus.getIndex(status);
            setCarPay(status);
            event.consume();
        });
        carPay.addEventHandler(ComboBox.ON_HIDDEN,event -> {
            int index = carPay.getSelectionModel().getSelectedIndex();
            if(index != lastCarPay && index != -1){
                lastCarPay = index;
                lastCarId = -1;
                pay = carPay.getSelectionModel().getSelectedItem();
                String[] s = pay.split(" ");
                pay = s[0];
                carId.getItems().clear();
                setCarId(brand,pay,status);
            }
            event.consume();
        });
        carStatus.addEventHandler(ComboBox.ON_SHOWING,event -> {
            if(carPay.getSelectionModel().getSelectedIndex() == -1){
                setCarStatus(null);
                return;
            }
            String pay = carPay.getSelectionModel().getSelectedItem();
            if(pay.isEmpty())
                pay = null;
            else {
                String[] s = pay.split(" ");
                pay = s[0];
            }
            setCarStatus(pay);
        });
        carStatus.addEventHandler(ComboBox.ON_HIDDEN,event -> {
            int index = carStatus.getSelectionModel().getSelectedIndex();
            if(index != lastCarStatus && index != -1){
                lastCarStatus = index;
                lastCarId = -1;
                status = "" + DBConnector.carStatus.getIndex(carStatus.getSelectionModel().getSelectedItem());
                carId.getItems().clear();
            }
            event.consume();
        });
        carId.addEventHandler(ComboBox.ON_SHOWING,event -> {
            String[] strings;
            if(carBrand.getEditor().getText().isEmpty()){
                setStatusLabel("请先选择车辆品牌");
                return;
            }
            brand = carBrand.getEditor().getText();
            int index = carPay.getSelectionModel().getSelectedIndex();
            if(index != -1){
                strings = carPay.getSelectionModel().getSelectedItem().split(" ");
                pay = strings[0];
            }else
                pay = null;
            index = carStatus.getSelectionModel().getSelectedIndex();
            if(index != -1) {
                status = "" + DBConnector.carStatus.getIndex(carStatus.getSelectionModel().getSelectedItem());
            }else
                status = null;
            setCarId(brand,pay,status);
            event.consume();
        });
        carId.addEventHandler(ComboBox.ON_HIDDEN,event -> {
            int index = carId.getSelectionModel().getSelectedIndex();
            if(index != -1 && index != lastCarId){
                lastCarId = index;
                license = carId.getItems().get(index);
                try{
                    String license = carId.getItems().get(index);
                    ResultSet resultSet = DBConnector.getInstance().getCarInfo(license);
                    while (resultSet.next()) {
                        if(carPay.getSelectionModel().getSelectedIndex() == -1) {
                            pay = "" + resultSet.getInt("cost");
                            carPay.getItems().add(resultSet.getInt("cost") + " 元/天");
                            carPay.setPromptText(resultSet.getInt("cost") + " 元/天");
                            lastCarPay = 0;
                        }
                        if(carStatus.getSelectionModel().getSelectedIndex() == -1) {
                            status = "" + resultSet.getInt("status");
                            carStatus.getItems().add(DBConnector.carStatus.getMsg(resultSet.getInt("status")));
                            carStatus.setPromptText(DBConnector.carStatus.getMsg(resultSet.getInt("status")));
                            lastCarStatus = 0;
                        }
                        carCost.setText(resultSet.getInt("peldge") + "元");
                        //carPay.setDisable(true);
                        //carStatus.setDisable(true);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            statusLabel.setText("");
            event.consume();
        });
        carName.addEventHandler(ComboBox.ON_HIDDEN,event -> {
            int index = carName.getSelectionModel().getSelectedIndex();
            if(index != lastCarName && index != -1){
                lastCarName = index;
                empId = carName.getItems().get(index);
                empId = "" + DBConnector.getInstance().getEmployeeID(empId);
            }
        });

        confirmButton.setOnMouseClicked(mouseEvent -> {
            clickedConfirmButton();
        });
        clearButton.setOnMouseClicked(mouseEvent -> {
            clickedClearButton();
        });
        exitButton.setOnMouseClicked(mouseEvent -> {
            clickedExitButton();
        });
        logoutButton.setOnMouseClicked(mouseEvent -> {
            clickedLogoutButton(logoutButton);
        });
        exitButton_2.setOnMouseClicked(mouseEvent -> {
            clickedExitButton();
        });
        logoutButton_2.setOnMouseClicked(mouseEvent -> {
            clickedLogoutButton(logoutButton_2);
        });
        returnCarButton.setOnMouseClicked(mouseEvent -> {
            clickedReturnCarButton();
        });
        rentTab.setOnSelectionChanged(event -> {
            clickedClearButton();
        });
        returnTab.setOnSelectionChanged(event -> {
            setRentCarInfo();
        });

    }

    /**
     * 设置选择汽车品牌输入框
     */
    private void setCarBrand(){
        String pingyin = carBrand.getEditor().getText();  //获取输入的拼音
        if(pingyin.isEmpty())
            pingyin = null;
        carBrand.getItems().clear();
        carBrand.getItems().addAll(DBConnector.getInstance().getCarBrand(pingyin));
    }

    /**
     * 设置汽车租金
     * @param status 汽车情况
     */
    private void setCarPay(String status){
        if(carBrand.getEditor().getText().isEmpty()){
            setStatusLabel("请先选择汽车品牌");
            return;
        }
        carPay.getItems().clear();
        carPay.getItems().addAll(DBConnector.getInstance().getCarPay(carBrand.getEditor().getText(),status));
    }

    /**
     * 设置汽车车况等级
     * @param pay 汽车租金
     */
    private void setCarStatus(String pay){
        if(carBrand.getEditor().getText().isEmpty()){
            setStatusLabel("请先选择汽车品牌");
            return;
        }
        carStatus.getItems().clear();
        carStatus.getItems().addAll(DBConnector.getInstance().getCarStatus(carBrand.getEditor().getText(),pay));
    }

    /**
     * 设置车牌
     * @param brand 品牌
     * @param cost 花费
     * @param status 状态
     */
    private void setCarId(String brand,String cost,String status){
        if(carBrand.getEditor().getText().isEmpty()){
            setStatusLabel("请先选择汽车品牌");
            return;
        }
        carId.getItems().clear();
        carId.getItems().addAll(DBConnector.getInstance().getCarId(brand,cost,status));
    }

    /**
     * 设置出错信息
     * @param warningMsg 出错提示
     */
    private void setStatusLabel(String warningMsg) {
        statusLabel.setText(warningMsg);
        statusLabel.setStyle("-fx-text-fill: #ff0000;");
    }

    /**
     * 租车事件
     */
    private void clickedConfirmButton(){
        if(carBrand.getSelectionModel().getSelectedIndex() < 0) {
            setStatusLabel("请选择汽车品牌");
            return;
        }
        if(carId.getSelectionModel().getSelectedIndex() < 0) {
            setStatusLabel("请选择汽车车牌号码");
            return;
        }
        if(lastCarStatus == -1) {
            setStatusLabel("请选择汽车状况");
            return;
        }
        if(lastCarPay == -1) {
            setStatusLabel("请选择汽车租金");
            return;
        }
        if(carName.getSelectionModel().getSelectedIndex() < 0) {
            setStatusLabel("请选择经手人");
            return;
        }
        int days = startDate.getValue().until(endDate.getValue()).getDays() + 1;
        double rate = DBConnector.getInstance().getMember(userId) ? 0.9 : 1;  //会员
        if((LocalDate.now().toEpochDay() - startDate.getValue().toEpochDay() > 0) || (startDate.getValue().toEpochDay() - endDate.getValue().toEpochDay() > 0) ){
            JOptionPane.showMessageDialog(null,"请重新选择合理的时间","租车系统",JOptionPane.ERROR_MESSAGE);
            return;
        }
        boolean falg = DBConnector.getInstance().rentCarTranc(license,userId, empId,(startDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " " + LocalDateTime.now().format(DateTimeFormatter.ofPattern(" HH:mm:ss"))),
                (endDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 23:00:00"),1, (int) ((int)days * Integer.parseInt(pay) * rate),"租车");
        if(falg) {
            JOptionPane.showMessageDialog(null, "预约租车成功!", "租车系统", JOptionPane.PLAIN_MESSAGE);
            clickedClearButton();
        }
        else{
            JOptionPane.showMessageDialog(null,"当前车辆已被预约，请重新选择车辆预约!","租车系统",JOptionPane.ERROR_MESSAGE);
            clickedClearButton();
        }
    }

    /**
     * 设置租车列表
     */
    private void setRentCarInfo(){
        carId_r.setCellValueFactory((TableColumn.CellDataFeatures<rentCarInfo, String> param) -> param.getValue().license);
        carBrand_r.setCellValueFactory((TableColumn.CellDataFeatures<rentCarInfo, String> param) -> param.getValue().brand);
        carStatus_r.setCellValueFactory((TableColumn.CellDataFeatures<rentCarInfo, String> param) -> param.getValue().status);
        carCost_r.setCellValueFactory((TableColumn.CellDataFeatures<rentCarInfo, String> param) -> param.getValue().cost);
        carYa_r.setCellValueFactory((TableColumn.CellDataFeatures<rentCarInfo, String> param) -> param.getValue().ya);
        carEndDate.setCellValueFactory((TableColumn.CellDataFeatures<rentCarInfo, String> param) -> param.getValue().endTime);
        rentCarInfoList.clear();
        rentCarInfoList.addAll(DBConnector.getInstance().getRentInfo(userId));
        rentInfo.setItems(rentCarInfoList);
    }

    /**
     * 还车事件
     */
    private void clickedReturnCarButton(){
        rentCarInfo rentCarInfo = rentInfo.getSelectionModel().getSelectedItem();
        if(rentCarInfo == null){
            JOptionPane.showMessageDialog(new JFrame().getContentPane(), "请选中要还的车辆！", "warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int profit = DBConnector.getInstance().returnCar(userId,rentCarInfo.license.getValue());
        if(profit == 0)
            JOptionPane.showMessageDialog(null,"还车成功","租车系统",JOptionPane.PLAIN_MESSAGE);
        else
            JOptionPane.showMessageDialog(null,("逾期还车,请补交逾期金额: "+profit+" 元"),"租车系统",JOptionPane.ERROR_MESSAGE);
        setRentCarInfo();
    }

    /**
     * 清除事件
     */
    private void clickedClearButton(){
        carBrand.getItems().clear();
        lastCarBrand = -1;
        brand = null;
        carPay.getItems().clear();
        lastCarPay = -1;
        pay = null;
        carName.getItems().clear();
        carName.getItems().addAll(DBConnector.getInstance().getEmployeeInfo());
        lastCarName = -1;
        empId = null;
        carStatus.getItems().clear();
        carStatus.setPromptText("");
        carPay.setPromptText("");
        lastCarStatus = -1;
        status = null;
        carId.getItems().clear();
        lastCarId = -1;
        license = null;

        startDate.setValue(LocalDate.now());
        endDate.setValue(LocalDate.now());
        carCost.setText("");
        statusLabel.setText("");
    }

    /**
     * 退出事件
     */
    private void clickedExitButton(){
        exit(0);
    }

    /**
     * 注销事件
     * @param exitButton 注销按钮
     */
    private void clickedLogoutButton(Button exitButton){
        try {
            Stage nextStage = (Stage) exitButton.getScene().getWindow();
            nextStage.close();
            Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
            Scene scene = new Scene(root, 598, 343);
            scene.setRoot(root);
            Stage stage = new Stage();
            scene.getStylesheets().add(getClass().getResource("login.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setUserName(String userName) {
        customerController.userName = userName;
    }

    public static void setUserId(String userId) {
        customerController.userId = userId;
    }
}
