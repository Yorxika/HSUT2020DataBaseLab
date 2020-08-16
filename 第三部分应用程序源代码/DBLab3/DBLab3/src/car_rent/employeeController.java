package car_rent;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

import static java.lang.System.exit;

public class employeeController {

    /**
     * 汽车使用情况内部类
     */
    static class carInfo{
        public StringProperty carlicense;
        public StringProperty carBrand;
        public StringProperty carStatus;
        public StringProperty carRent;
        public StringProperty carYa;
        public StringProperty carValid;

        public carInfo(String license,String brand,int status,int rent,int ya,int valid){
            carlicense = new SimpleStringProperty(license);
            carBrand = new SimpleStringProperty(brand);
            carStatus = new SimpleStringProperty(DBConnector.carStatus.getMsg(status));
            carRent = new SimpleStringProperty(rent + " 元/天");
            carYa = new SimpleStringProperty(ya + " 元");
            carValid = new SimpleStringProperty(valid == 1 ? "空闲" : "租借中");
        }
    }

    /**
     * 用户信息内部类
     */
    static class cusInfo{
        public StringProperty cusId;
        public StringProperty cusName;
        public StringProperty isMember;
        public StringProperty cusRedit;
        public StringProperty cusPasswd;
        public StringProperty cusLastLogin;

        public cusInfo(int id,String name,int member,int redit,String passwd,String lastLog){
            cusId = new SimpleStringProperty("" + id);
            cusName = new SimpleStringProperty(name);
            isMember = new SimpleStringProperty(member == 1 ? "会员" : "非会员");
            cusRedit = new SimpleStringProperty(DBConnector.redit.getMsg(redit));
            cusPasswd = new SimpleStringProperty(passwd);
            cusLastLogin = new SimpleStringProperty(lastLog);
        }
    }

    /**
     * 员工信息内部类
     */
    static class employeeInfo{
        public StringProperty empId;
        public StringProperty empName;
        public StringProperty empTitle;
        public StringProperty empPasswd;
        public StringProperty empLastLogin;

        public employeeInfo(int id,String name,int title,String passwd,String dlrq){
            empId = new SimpleStringProperty("" + id);
            empName = new SimpleStringProperty(name);
            empTitle = new SimpleStringProperty(DBConnector.title.getName(title));
            empPasswd = new SimpleStringProperty(passwd);
            empLastLogin = new SimpleStringProperty(dlrq);
        }
    }

    /**
     * 日志信息内部类
     */
    static class diaryInfo{
        public StringProperty diaId;
        public StringProperty license;
        public StringProperty cusName;
        public StringProperty empName;
        public StringProperty starttime;
        public StringProperty endtime;
        public StringProperty event;
        public StringProperty profit;
        public StringProperty detail;

        public diaryInfo(String id,String car,String cus,String emp,String start,String end,int event,int profit,String detail){
            diaId = new SimpleStringProperty(id);
            license = new SimpleStringProperty(car);
            cusName = new SimpleStringProperty(cus);
            empName = new SimpleStringProperty(emp);
            starttime = new SimpleStringProperty(start);
            endtime = new SimpleStringProperty(end);
            this.event = new SimpleStringProperty(DBConnector.event.getMsg(event));
            this.profit = new SimpleStringProperty(profit + " 元");
            this.detail = new SimpleStringProperty(detail);
        }
    }

    @FXML
    private Tab carInfoTab;
    @FXML
    private TableView<carInfo> carInfoTable;
    @FXML
    private TableColumn<carInfo,String> carId;
    @FXML
    private TableColumn<carInfo,String> carBrand;
    @FXML
    private TableColumn<carInfo,String> carStatus;
    @FXML
    private TableColumn<carInfo,String> carRent;
    @FXML
    private TableColumn<carInfo,String> carYa;
    @FXML
    private TableColumn<carInfo,String> carValid;

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label statusLabel;

    @FXML
    private TextField newCarLicense;
    @FXML
    private TextField newCarBrand;
    @FXML
    private TextField newCarRent;
    @FXML
    private TextField newCarYa;
    @FXML
    private ChoiceBox<String> newCarStatus;

    @FXML
    private Button logoutButton;
    @FXML
    private Button exitButton;
    @FXML
    private Button modifyCarButton;
    @FXML
    private Button addCarButton;
    @FXML
    private Button deleteCarButton;

    @FXML
    private Tab cusInfoTab;
    @FXML
    private TableView<cusInfo> cusInfoTable;
    @FXML
    private TableColumn<cusInfo,String> cusId;
    @FXML
    private TableColumn<cusInfo,String> cusName;
    @FXML
    private TableColumn<cusInfo,String> cusPasswd;
    @FXML
    private TableColumn<cusInfo,String> cusRedit;
    @FXML
    private TableColumn<cusInfo,String> cusMember;
    @FXML
    private TableColumn<cusInfo,String> cusLastLogin;

    @FXML
    private Label statusLabel_2;

    @FXML
    private TextField newCusId;
    @FXML
    private TextField newCusName;
    @FXML
    private TextField newCusPasswd;
    @FXML
    private ChoiceBox<String> newCusMember;

    @FXML
    private Button logoutButton_2;
    @FXML
    private Button exitButton_2;
    @FXML
    private Button modifyCusButton;
    @FXML
    private Button deleteCusButton;
    @FXML
    private Button addCusButton;

    @FXML
    private Tab empInfoTab;
    @FXML
    private TableView<employeeInfo> empInfoTable;
    @FXML
    private TableColumn<employeeInfo,String> empId;
    @FXML
    private TableColumn<employeeInfo,String> empName;
    @FXML
    private TableColumn<employeeInfo,String> empTitle;
    @FXML
    private TableColumn<employeeInfo,String> empPasswd;
    @FXML
    private TableColumn<employeeInfo,String> empLastLogin;

    @FXML
    private Label statusLabel_3;

    @FXML
    private TextField newEmpId;
    @FXML
    private TextField newEmpName;
    @FXML
    private TextField newEmpPasswd;
    @FXML
    private TextField newEmpTitle;


    @FXML
    private Button logoutButton_3;
    @FXML
    private Button exitButton_3;
    @FXML
    private Button modifyEmpButton;
    @FXML
    private Button addEmpButton;
    @FXML
    private Button deleteEmpButton;

    @FXML
    private Tab diaInfoTab;
    @FXML
    private TableView<diaryInfo> diaInfoTable;
    @FXML
    private TableColumn<diaryInfo,String> diaId;
    @FXML
    private TableColumn<diaryInfo,String> diaCarId;
    @FXML
    private TableColumn<diaryInfo,String> diaCusName;
    @FXML
    private TableColumn<diaryInfo,String> diaEmpName;
    @FXML
    private TableColumn<diaryInfo,String> diaStime;
    @FXML
    private TableColumn<diaryInfo,String> diaEtime;
    @FXML
    private TableColumn<diaryInfo,String> diaEvent;
    @FXML
    private TableColumn<diaryInfo,String> diaProfit;
    @FXML
    private TableColumn<diaryInfo,String> diaDetail;

    @FXML
    private Label statusLabel_4;

    @FXML
    private TextField newDiaCar;
    @FXML
    private TextField newDiaCus;
    @FXML
    private TextField newDiaEmp;
    @FXML
    private TextField newDiaEvent;
    @FXML
    private TextField newDiaProfit;
    @FXML
    private TextField newDiaDetail;

    @FXML
    private Button logoutButton_4;
    @FXML
    private Button exitButton_4;
    @FXML
    private Button writeDiaButton;
    @FXML
    private Button deleteDiaButton;


    @FXML
    private Tab charTab;
    @FXML
    private ChoiceBox<String> typeChoice;
    @FXML
    private ChoiceBox<String> timeChoice;
    @FXML
    private Button showChartButton;
    @FXML
    private Button logoutButton_5;
    @FXML
    private Button exitButton_5;
    @FXML
    private DatePicker sTime;
    @FXML
    private DatePicker eTime;
    @FXML
    private LineChart<String,Number> lineChart;

    private static String userId;
    private static String userName;
    private static int title;  //职称对应功能

    private ObservableList<carInfo> carInfoList = FXCollections.observableArrayList();
    private ObservableList<cusInfo> cusInfoList = FXCollections.observableArrayList();
    private ObservableList<employeeInfo> empInfoList = FXCollections.observableArrayList();
    private ObservableList<diaryInfo> diaryInfoList = FXCollections.observableArrayList();

    @FXML
    private void initialize(){
        welcomeLabel.setText("欢迎登录租车系统," + userName);
        statusLabel.setText("");
        setCarInfo();

        carInfoTab.setOnSelectionChanged(event -> {
            setCarInfo();
            statusLabel.setText("");
        });
        cusInfoTab.setOnSelectionChanged(event -> {
            setCusInfo();
            statusLabel_2.setText("");
        });
        empInfoTab.setOnSelectionChanged(event -> {
            setEmpInfo();
            statusLabel_3.setText("");
        });
        diaInfoTab.setOnSelectionChanged(event -> {
            setDiaInfo();
            newDiaEmp.setText(userName);
            statusLabel_4.setText("");
        });
        charTab.setOnSelectionChanged(event -> {
            sTime.setValue(LocalDate.now());
            eTime.setValue(LocalDate.now());
            lineChart.getData().clear();
            lineChart.setVisible(false);
        });

        logoutButton.setOnMouseClicked(mouseEvent -> {
            clickedLogoutButton(logoutButton);
        });
        logoutButton_2.setOnMouseClicked(mouseEvent -> {
            clickedLogoutButton(logoutButton_2);
        });
        logoutButton_3.setOnMouseClicked(mouseEvent -> {
            clickedLogoutButton(logoutButton_3);
        });
        logoutButton_4.setOnMouseClicked(mouseEvent -> {
            clickedLogoutButton(logoutButton_4);
        });
        logoutButton_5.setOnMouseClicked(mouseEvent -> {
            clickedLogoutButton(logoutButton_5);
        });
        exitButton.setOnMouseClicked(mouseEvent -> {
            clickedExitButton();
        });
        exitButton_2.setOnMouseClicked(mouseEvent -> {
            clickedExitButton();
        });
        exitButton_3.setOnMouseClicked(mouseEvent -> {
            clickedExitButton();
        });
        exitButton_4.setOnMouseClicked(mouseEvent -> {
            clickedExitButton();
        });
        exitButton_5.setOnMouseClicked(mouseEvent -> {
            clickedExitButton();
        });
        carInfoTable.setOnMouseClicked(mouseEvent -> {
            carInfo carInfo = carInfoTable.getSelectionModel().getSelectedItem();
            if(carInfo == null)
                return;
            String[] s;
            newCarLicense.setText(carInfo.carlicense.getValue());
            newCarBrand.setText(carInfo.carBrand.getValue());
            newCarStatus.setValue(carInfo.carStatus.getValue());
            s = carInfo.carRent.getValue().split(" ");
            newCarRent.setText(s[0]);
            s = carInfo.carYa.getValue().split(" ");
            newCarYa.setText(s[0]);

        });
        modifyCarButton.setOnMouseClicked(mouseEvent -> {
            clickedModifyCarButton();
        });
        addCarButton.setOnMouseClicked(mouseEvent -> {
            clickedNewCarButton();
        });
        deleteCarButton.setOnMouseClicked(mouseEvent -> {
            clickedDeleteCarButton();
        });
        newCarRent.setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER || keyEvent.getCode() == KeyCode.BACK_SPACE)
                return;
           checkInputDigit(statusLabel, newCarRent);
        });
        newCarYa.setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER || keyEvent.getCode() == KeyCode.BACK_SPACE)
                return;
            checkInputDigit(statusLabel, newCarYa);
        });
        newCusId.setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER || keyEvent.getCode() == KeyCode.BACK_SPACE)
                return;
            checkInputDigit(statusLabel_2, newCusId);
        });
        modifyCusButton.setOnMouseClicked(mouseEvent -> {
            clickedModifyCusButton();
        });
        addCusButton.setOnMouseClicked(mouseEvent -> {
            clickedNewCusButton();
        });
        deleteCusButton.setOnMouseClicked(mouseEvent -> {
            clickedDeleteCus();
        });
        cusInfoTable.setOnMouseClicked(mouseEvent -> {
            cusInfo cusInfo = cusInfoTable.getSelectionModel().getSelectedItem();
            if(cusInfo == null)
                return;
            newCusId.setText(cusInfo.cusId.getValue());
            newCusName.setText(cusInfo.cusName.getValue());
            newCusPasswd.setText(cusInfo.cusPasswd.getValue());
            newCusMember.setValue(cusInfo.isMember.getValue());
        });
        empInfoTable.setOnMouseClicked(mouseEvent -> {
            employeeInfo empIfo = empInfoTable.getSelectionModel().getSelectedItem();
            if(empIfo == null)
                return;
            newEmpId.setText(empIfo.empId.getValue());
            newEmpName.setText(empIfo.empName.getValue());
            newEmpPasswd.setText(empIfo.empPasswd.getValue());
            newEmpTitle.setText(empIfo.empTitle.getValue());
        });
        modifyEmpButton.setOnMouseClicked(mouseEvent -> {
            clickedModifyEmpButton();
        });
        deleteEmpButton.setOnMouseClicked(mouseEvent -> {
            clickedDeleteEmpButton();
        });
        addEmpButton.setOnMouseClicked(mouseEvent -> {
            clickedAddEmpBuuton();
        });
        newDiaProfit.setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER || keyEvent.getCode() == KeyCode.BACK_SPACE)
                return;
            checkInputDigit(statusLabel_4,newDiaProfit);
        });
        writeDiaButton.setOnMouseClicked(mouseEvent -> {
            clickedWriteDiaButton();
        });
        deleteDiaButton.setOnMouseClicked(mouseEvent -> {
            clickedDeleteDiaButton();
        });
        diaInfoTable.setOnMouseClicked(mouseEvent -> {
            diaryInfo tmp = diaInfoTable.getSelectionModel().getSelectedItem();
            if(tmp == null)
                return;
            newDiaCar.setText(tmp.diaId.getValue());
            newDiaCus.setText(tmp.cusName.getValue());
            //newDiaEmp.setText(userName);
        });
        showChartButton.setOnMouseClicked(mouseEvent -> {
            clickedShowChartButton();
        });
    }

    /**
     * 获取车辆信息
     */
    private void setCarInfo() {
        carId.setCellValueFactory((TableColumn.CellDataFeatures<carInfo, String> param) -> param.getValue().carlicense);
        carBrand.setCellValueFactory((TableColumn.CellDataFeatures<carInfo, String> param) -> param.getValue().carBrand);
        carStatus.setCellValueFactory((TableColumn.CellDataFeatures<carInfo, String> param) -> param.getValue().carStatus);
        carRent.setCellValueFactory((TableColumn.CellDataFeatures<carInfo, String> param) -> param.getValue().carRent);
        carYa.setCellValueFactory((TableColumn.CellDataFeatures<carInfo, String> param) -> param.getValue().carYa);
        carValid.setCellValueFactory((TableColumn.CellDataFeatures<carInfo, String> param) -> param.getValue().carValid);
        carInfoList.clear();
        carInfoList.addAll(DBConnector.getInstance().getCarInfo());
        carInfoTable.setItems(carInfoList);
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
            Scene scene = new Scene(root);
            scene.setRoot(root);
            Stage stage = new Stage();
            scene.getStylesheets().add(getClass().getResource("login.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改车辆信息事件
     */
    private void clickedModifyCarButton(){
        if(!checkTitle("modifyCar"))
            return;
        if(newCarLicense.getText().isEmpty() || newCarBrand.getText().isEmpty() || newCarRent.getText().isEmpty() || newCarYa.getText().isEmpty()){
            setStatusLabel(statusLabel,"请填完该车辆的所有信息");
            return;
        }
        if(!checkInputDigit(statusLabel, newCarYa) || !checkInputDigit(statusLabel, newCarRent)){
            return;
        }
        if(!DBConnector.getInstance().modifyCar(newCarLicense.getText(),newCarBrand.getText(),Integer.parseInt(newCarRent.getText().split(" ")[0]),
                DBConnector.carStatus.getIndex(newCarStatus.getValue()),Integer.parseInt(newCarYa.getText().split(" ")[0])))
            JOptionPane.showMessageDialog(null,"该车辆正在租借中，无法修改","Warning",JOptionPane.ERROR_MESSAGE);
        else {
            JOptionPane.showMessageDialog(null, "修改车辆信息成功", "Success", JOptionPane.PLAIN_MESSAGE);
            setCarInfo();
        }

    }

    /**
     * 增加车辆操作
     */
    private void clickedNewCarButton(){
        if(!checkTitle("addCar"))
            return;
        if(newCarLicense.getText().isEmpty() || newCarBrand.getText().isEmpty() || newCarRent.getText().isEmpty() || newCarYa.getText().isEmpty()){
            setStatusLabel(statusLabel,"请填完该车辆的所有信息");
            return;
        }
        if(!checkInputDigit(statusLabel, newCarYa) || !checkInputDigit(statusLabel, newCarRent)){
            return;
        }
        if(newCarStatus.getSelectionModel().getSelectedIndex() < 0){
            setStatusLabel(statusLabel, "请选择汽车状态");
            return;
        }
        if(!DBConnector.getInstance().addCar(newCarLicense.getText(),newCarBrand.getText(),Integer.parseInt(newCarRent.getText().split(" ")[0]),
                DBConnector.carStatus.getIndex(newCarStatus.getValue()),Integer.parseInt(newCarYa.getText().split(" ")[0])))
            JOptionPane.showMessageDialog(null,"不能添加重复车辆","Warning",JOptionPane.ERROR_MESSAGE);
        else {
            JOptionPane.showMessageDialog(null, "添加车辆成功", "Success", JOptionPane.PLAIN_MESSAGE);
            setCarInfo();
        }
    }

    /**
     * 删除车辆事件
     */
    private void clickedDeleteCarButton(){
        if(!checkTitle("deleteCar"))
            return;
        carInfo carInfo = carInfoTable.getSelectionModel().getSelectedItem();
        if(carInfo == null){
            JOptionPane.showMessageDialog(new JFrame().getContentPane(), "请选中要删除的车辆！", "warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if(!DBConnector.getInstance().deleteCar(carInfo.carlicense.getValue()))
            JOptionPane.showMessageDialog(new JFrame().getContentPane(), "该车辆正在租借中,删除失败!", "warning", JOptionPane.ERROR_MESSAGE);
        else {
            JOptionPane.showMessageDialog(new JFrame().getContentPane(), "删除车辆成功!", "success", JOptionPane.PLAIN_MESSAGE);
            setCarInfo();
        }
    }

    /**
     * 修改客户信息事件
     */
    private void clickedModifyCusButton(){
        if(!checkTitle("modifyMember"))
            return;
        if(newCusId.getText().isEmpty() || newCusName.getText().isEmpty() || newCusPasswd.getText().isEmpty()){
            setStatusLabel(statusLabel_2,"请填完新客户的所有信息");
            return;
        }
        if(!checkInputDigit(statusLabel_2, newCusId))
            return;
        if(!DBConnector.getInstance().modifyCus(newCusId.getText().trim(),newCusName.getText().trim(),newCusPasswd.getText().trim(),newCusMember.getValue())){
            JOptionPane.showMessageDialog(null,"该客户不存在","warning",JOptionPane.ERROR_MESSAGE);
        }
        else {
            JOptionPane.showMessageDialog(null, "修改客户信息成功", "Success", JOptionPane.PLAIN_MESSAGE);
            setCusInfo();
        }

    }

    /**
     * 增加用户事件
     */
    private void clickedNewCusButton(){
        if(!checkTitle("addMember"))
            return;
        if(newCusId.getText().isEmpty() || newCusName.getText().isEmpty() || newCusPasswd.getText().isEmpty()){
            setStatusLabel(statusLabel_2,"请填完新客户的所有信息");
            return;
        }
        if(!checkInputDigit(statusLabel_2, newCusId))
            return;
        if(!DBConnector.getInstance().addNewCus(newCusId.getText().trim(),newCusName.getText().trim(),newCusPasswd.getText().trim(),newCusMember.getValue())){
            JOptionPane.showMessageDialog(null,"不能添加重复客户","warning",JOptionPane.ERROR_MESSAGE);
        }
        else {
            JOptionPane.showMessageDialog(null, "添加新客户成功", "Success", JOptionPane.PLAIN_MESSAGE);
            setCusInfo();
        }
    }

    /**
     * 删除用户事件
     */
    private void clickedDeleteCus(){
        if(!checkTitle("deleteMember"))
            return;
        cusInfo cusInfo = cusInfoTable.getSelectionModel().getSelectedItem();
        if(cusInfo == null){
            JOptionPane.showMessageDialog(null, "请选中要删除的客户！", "warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if(!DBConnector.getInstance().deleteCus(cusInfo.cusId.getValue()))
            JOptionPane.showMessageDialog(null, "该用户仍在租车中,删除失败!", "warning", JOptionPane.ERROR_MESSAGE);
        else{
            JOptionPane.showMessageDialog(null, "删除用户成功!", "success", JOptionPane.PLAIN_MESSAGE);
            setCusInfo();
        }
    }

    /**
     * 获取客户信息
     */
    private void setCusInfo(){
        cusId.setCellValueFactory((TableColumn.CellDataFeatures<cusInfo, String> param) -> param.getValue().cusId);
        cusName.setCellValueFactory((TableColumn.CellDataFeatures<cusInfo, String> param) -> param.getValue().cusName);
        cusMember.setCellValueFactory((TableColumn.CellDataFeatures<cusInfo, String> param) -> param.getValue().isMember);
        cusLastLogin.setCellValueFactory((TableColumn.CellDataFeatures<cusInfo, String> param) -> param.getValue().cusLastLogin);
        cusPasswd.setCellValueFactory((TableColumn.CellDataFeatures<cusInfo, String> param) -> param.getValue().cusPasswd);
        cusRedit.setCellValueFactory((TableColumn.CellDataFeatures<cusInfo, String> param) -> param.getValue().cusRedit);
        cusInfoList.clear();
        cusInfoList.addAll(DBConnector.getInstance().getCusInfo());
        cusInfoTable.setItems(cusInfoList);
    }

    /**
     * 获取员工信息
     */
    private void setEmpInfo(){
        empId.setCellValueFactory((TableColumn.CellDataFeatures<employeeInfo, String> param) -> param.getValue().empId);
        empName.setCellValueFactory((TableColumn.CellDataFeatures<employeeInfo, String> param) -> param.getValue().empName);
        empTitle.setCellValueFactory((TableColumn.CellDataFeatures<employeeInfo, String> param) -> param.getValue().empTitle);
        empPasswd.setCellValueFactory((TableColumn.CellDataFeatures<employeeInfo, String> param) -> param.getValue().empPasswd);
        empLastLogin.setCellValueFactory((TableColumn.CellDataFeatures<employeeInfo, String> param) -> param.getValue().empLastLogin);
        empInfoList.clear();
        empInfoList.addAll(DBConnector.getInstance().getEmpInfo(userId,title));
        empInfoTable.setItems(empInfoList);
    }

    /**
     * 获取日志信息
     */
    private void setDiaInfo(){
        diaId.setCellValueFactory((TableColumn.CellDataFeatures<diaryInfo, String> param) -> param.getValue().diaId);
        diaCarId.setCellValueFactory((TableColumn.CellDataFeatures<diaryInfo, String> param) -> param.getValue().license);
        diaCusName.setCellValueFactory((TableColumn.CellDataFeatures<diaryInfo, String> param) -> param.getValue().cusName);
        diaEmpName.setCellValueFactory((TableColumn.CellDataFeatures<diaryInfo, String> param) -> param.getValue().empName);
        diaStime.setCellValueFactory((TableColumn.CellDataFeatures<diaryInfo, String> param) -> param.getValue().starttime);
        diaEtime.setCellValueFactory((TableColumn.CellDataFeatures<diaryInfo, String> param) -> param.getValue().endtime);
        diaEvent.setCellValueFactory((TableColumn.CellDataFeatures<diaryInfo, String> param) -> param.getValue().event);
        diaProfit.setCellValueFactory((TableColumn.CellDataFeatures<diaryInfo, String> param) -> param.getValue().profit);
        diaDetail.setCellValueFactory((TableColumn.CellDataFeatures<diaryInfo, String> param) -> param.getValue().detail);
        diaryInfoList.clear();
        diaryInfoList.addAll(DBConnector.getInstance().getDiaInfo(userId,title));
        diaInfoTable.setItems(diaryInfoList);
    }

    /**
     * 修改员工信息事件
     */
    private void clickedModifyEmpButton(){
        if(!checkTitle("modifyEmployee"))
            return;
        if(newEmpId.getText().isEmpty() || newEmpName.getText().isEmpty() || newEmpTitle.getText().isEmpty() || newEmpPasswd.getText().isEmpty()){
            setStatusLabel(statusLabel_3,"请填完待修改员工的所有信息");
            return;
        }
        if(!checkInputDigit(statusLabel_3, newEmpId))
            return;
        if(newEmpId.getText().trim().equals(userId)){
            JOptionPane.showMessageDialog(null, "无权修改自己的信息", "warning", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int status = DBConnector.getInstance().modifyEmp(newEmpId.getText().trim(),newEmpName.getText().trim(),newEmpTitle.getText().trim(),newEmpPasswd.getText().trim(),title);
        switch (status){
            case -10:
                JOptionPane.showMessageDialog(null, "数据库错误", "warning", JOptionPane.ERROR_MESSAGE);
                break;
            case -2:
                JOptionPane.showMessageDialog(null, "输入的职称无效", "warning", JOptionPane.ERROR_MESSAGE);
                break;
            case -1:
                JOptionPane.showMessageDialog(null, "该员工不存在", "warning", JOptionPane.ERROR_MESSAGE);
                break;
            case 0:
                JOptionPane.showMessageDialog(null, "没有权利修改更高的职称", "warning", JOptionPane.ERROR_MESSAGE);
                break;
            case 1:
                JOptionPane.showMessageDialog(null, "修改员工信息成功", "success", JOptionPane.PLAIN_MESSAGE);
                setEmpInfo();
                break;
        }
    }

    /**
     * 添加新员工
     */
    private void clickedAddEmpBuuton(){
        if(!checkTitle("addEmployee"))
            return;
        if(newEmpId.getText().isEmpty() || newEmpName.getText().isEmpty() || newEmpTitle.getText().isEmpty() || newEmpPasswd.getText().isEmpty()){
            setStatusLabel(statusLabel_3,"请填完待修改员工的所有信息");
            return;
        }
        if(!checkInputDigit(statusLabel_3, newEmpId))
            return;
        int status = DBConnector.getInstance().addEmp(newEmpId.getText().trim(),newEmpName.getText().trim(),newEmpTitle.getText().trim(),newEmpPasswd.getText().trim(),title);
        switch (status){
            case -10:
                JOptionPane.showMessageDialog(null, "数据库错误", "warning", JOptionPane.ERROR_MESSAGE);
                break;
            case -2:
                JOptionPane.showMessageDialog(null, "输入的职称无效", "warning", JOptionPane.ERROR_MESSAGE);
                break;
            case -1:
                JOptionPane.showMessageDialog(null, "该员工已存在", "warning", JOptionPane.ERROR_MESSAGE);
                break;
            case 0:
                JOptionPane.showMessageDialog(null, "没有权利创建更高的职称", "warning", JOptionPane.ERROR_MESSAGE);
                break;
            case 1:
                JOptionPane.showMessageDialog(null, "增加新员工成功", "success", JOptionPane.PLAIN_MESSAGE);
                setEmpInfo();
                break;
        }
    }

    /**
     * 删除员工事件
     */
    private void clickedDeleteEmpButton(){
        if(!checkTitle("deleteEmployee"))
            return;
        employeeInfo empInfo = empInfoTable.getSelectionModel().getSelectedItem();
        if(empInfo == null){
            JOptionPane.showMessageDialog(null, "请选中要删除的员工", "warning", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int status = DBConnector.getInstance().deleteEmp(newEmpId.getText().trim(),newEmpTitle.getText(),title);
        switch (status){
            case -10:
                JOptionPane.showMessageDialog(null, "数据库错误", "warning", JOptionPane.ERROR_MESSAGE);
                break;
            case -2:
                JOptionPane.showMessageDialog(null, "输入的职称无效", "warning", JOptionPane.ERROR_MESSAGE);
                break;
            case -1:
                JOptionPane.showMessageDialog(null, "不存在该员工", "warning", JOptionPane.ERROR_MESSAGE);
                break;
            case 0:
                JOptionPane.showMessageDialog(null, "无权利删除同级或者更高级的员工", "warning", JOptionPane.ERROR_MESSAGE);
                break;
            case 1:
                JOptionPane.showMessageDialog(null, "删除员工成功", "success", JOptionPane.PLAIN_MESSAGE);
                setEmpInfo();
                break;
        }
    }

    /**
     * 写日志事件
     */
    private void clickedWriteDiaButton(){
        if(!checkTitle("writeDiary"))
            return;
        newDiaEmp.setText(userName);
        if(newDiaCar.getText().isEmpty() || newDiaCus.getText().isEmpty() || newDiaProfit.getText().isEmpty() || newDiaEvent.getText().isEmpty()){
            setStatusLabel(statusLabel_4,"请填完日志的所有信息");
            return;
        }
        if(!checkInputDigit(statusLabel_4,newDiaProfit)){
            return;
        }
        try {
            ResultSet resultSet = DBConnector.getInstance().getCarInfo(newDiaCar.getText().trim());
            if(!resultSet.next()){
                JOptionPane.showMessageDialog(null, "车库里不存在这辆车", "warning", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int cusId = DBConnector.getInstance().getCusId(newDiaCus.getText());
            if(cusId < 0){
                JOptionPane.showMessageDialog(null, "不存在该客户", "warning", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int event = DBConnector.event.getId(newDiaEvent.getText());
            if (event < 0){
                JOptionPane.showMessageDialog(null, "不存在这样的事件", "warning", JOptionPane.ERROR_MESSAGE);
                return;
            }
            DBConnector.getInstance().setDiary(newDiaCar.getText(),("" + cusId),userId, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-ss-dd hh:mm:ss")),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-ss-dd hh:mm:ss")),event,Integer.parseInt(newDiaProfit.getText()),newDiaDetail.getText());
            JOptionPane.showMessageDialog(null, "添加日志成功", "Success", JOptionPane.PLAIN_MESSAGE);
            setDiaInfo();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 删除日志事件
     */
    private void clickedDeleteDiaButton(){
        if(!checkTitle("deleteDiary"))
            return;
        diaryInfo tmp = diaInfoTable.getSelectionModel().getSelectedItem();
        if(tmp == null){
            JOptionPane.showMessageDialog(null, "请选中要删除的日志", "warning", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if(!DBConnector.getInstance().deleteDiary(tmp.diaId.getValue())){
            JOptionPane.showMessageDialog(null, "数据库错误", "warning", JOptionPane.ERROR_MESSAGE);
        }else{
            JOptionPane.showMessageDialog(null, "删除日志成功", "success", JOptionPane.PLAIN_MESSAGE);
            setDiaInfo();
        }
    }

    /**
     * 显示报表操作
     */
    @SuppressWarnings("unchecked")
    private void clickedShowChartButton(){
        if(!checkTitle(typeChoice.getValue()))
            return;
        if((sTime.getValue().toEpochDay() - eTime.getValue().toEpochDay() > 0) ){
            JOptionPane.showMessageDialog(null,"请重新选择合理的时间","租车系统",JOptionPane.ERROR_MESSAGE);
            return;
        }
        int level = DBConnector.timeType.getId(timeChoice.getValue());
        if(typeChoice.getValue().equals("个人报表")){
            lineChart.getData().clear();
            lineChart.setTitle("个人" +timeChoice.getValue().substring(0,1)+"收入报表");
            lineChart.getData().addAll(DBConnector.getInstance().getPersonChart(userId,level,
                    sTime.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))+" 00:00:00",eTime.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +" 23:59:59"));
            lineChart.setVisible(true);
        }
        if(typeChoice.getValue().equals("车辆报表")){
            lineChart.getData().clear();
            lineChart.setTitle("公司所有车辆" +timeChoice.getValue().substring(0,1)+"收入报表");
            //lineChart.getData().addAll(DBConnector.getInstance().getCarChart(level));
            LinkedList<XYChart.Series<String ,Number>> ans = DBConnector.getInstance().getCarChart(level,
                    sTime.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))+" 00:00:00",eTime.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))+" 23:59:59");
            for(XYChart.Series<String ,Number> xy : ans)
                lineChart.getData().addAll(xy);
            lineChart.setVisible(true);
        }
        if(typeChoice.getValue().equals("公司报表")){
            lineChart.getData().clear();
            lineChart.setTitle("公司" +timeChoice.getValue().substring(0,1)+"收入报表");
            lineChart.getData().addAll(DBConnector.getInstance().getYearChart(level,
                    sTime.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))+" 00:00:00",eTime.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))+" 23:59:59"));
            lineChart.setVisible(true);
        }
    }


    /**
     * 设置出错信息
     * @param label 标签
     * @param warningMsg 出错提示
     */
    private void setStatusLabel(Label label, String warningMsg) {
        label.setText(warningMsg);
        label.setStyle("-fx-text-fill: #ff0000;");
    }

    /**
     * 确认输入为数字
     * @param label 标签
     * @param textField 输入
     * @return 输入结果
     */
    private boolean checkInputDigit(Label label, TextField textField){
        String s = textField.getText().trim();
        if(!s.matches("[1-9]+[0-9]*|0")){
            setStatusLabel(label, "输入非法，请重新输入");
            return false;
        }
        setStatusLabel(label, "");
        return true;
    }

    /**
     * 确认员工操作
     * @param action 操作
     * @return 是否有权限
     */
    private boolean checkTitle(String action){
        int needTitle = DBConnector.employeeTitle.getTitle(action);
        if(needTitle > title){
            JOptionPane.showMessageDialog(null,("权限不够!"),"Warning",JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * 退出事件
     */
    private void clickedExitButton(){
        exit(0);
    }

    public static void setUserId(String userId) {
        employeeController.userId = userId;
    }

    public static void setUserName(String userName) {
        employeeController.userName = userName;
    }

    public static void setTitle(int title) {
        employeeController.title = title;
    }
}
