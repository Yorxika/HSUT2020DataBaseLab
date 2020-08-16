package car_rent;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Random;

public class DBConnector {
    private static volatile DBConnector instance = null;
    private Connection connection;
    private Statement statement;

    /**
     * 枚举汽车情况
     */
    public enum carStatus{
        A(1,"综合车况很好"),
        B(2,"综合车况较好"),
        C(3,"综合车况一般"),
        D(4,"综合车况较差"),
        E(5,"综合车况很差");

        int statusLevel;
        String msg;

        carStatus(int statusLevel, String msg){
            this.statusLevel = statusLevel;
            this.msg = msg;
        }

        public static String getMsg(int index){
            for(carStatus tmp : carStatus.values()){
                if(tmp.statusLevel == index)
                    return tmp.msg;
            }
            return null;
        }

        public static int getIndex(String msg){
            for (carStatus tmp : carStatus.values()){
                if(tmp.msg.equals(msg))
                    return tmp.statusLevel;
            }
            return -1;
        }

    }

    /**
     * 职工权利对照表
     */
    public enum employeeTitle{
        A(2,"addCar"),
        B(2,"deleteCar"),
        L(2,"modifyCar"),
        C(1,"addMember"),
        D(1,"deleteMember"),
        M(1,"modifyMember"),
        E(2,"addEmployee"),
        F(2,"deleteEmployee"),
        N(2,"modifyEmployee"),
        G(1,"writeDiary"),
        H(2,"deleteDiary"),
        I(1,"个人报表"),
        J(2,"车辆报表"),
        K(3,"公司报表");

        int title;
        String action;

        employeeTitle(int title, String action) {
            this.title = title;
            this.action = action;
        }

        public static int getTitle(String action){
            for (employeeTitle tmp : employeeTitle.values())
                if(tmp.action.equals(action))
                    return tmp.title;
                return -1;
        }
    }

    /**
     * 枚举职工title
     */
    public enum title{
        A(1,"员工"),
        B(2,"主管"),
        C(3,"经理"),
        Z(Integer.MAX_VALUE,"超级管理员");

        int title;
        String name;

        title(int title,String name){
            this.title = title;
            this.name = name;
        }

        public static String getName(int index){
            for (title tmp : DBConnector.title.values()){
                if(tmp.title == index)
                    return tmp.name;
            }
            return null;
        }

        public static int getTitle(String name){
            for(DBConnector.title tmp : DBConnector.title.values()){
                if(tmp.name.equals(name))
                    return tmp.title;
            }
            return -1;
        }

    }

    /**
     * 枚举信用常量
     */
    public enum redit{
        A(0,"信用优良"),
        B(2,"信用良好"),
        C(4,"信用一般"),
        D(10,"信用不佳");

        int grade;
        String msg;

        redit(int grade, String msg) {
            this.grade = grade;
            this.msg = msg;
        }

        public static String getMsg(int grade){
            for (redit tmp : redit.values())
                if(grade <= tmp.grade)
                    return tmp.msg;
                return D.msg;
        }
    }

    /**
     * 枚举事件类型
     */
    public enum event{
        A(1,"租车"),
        B(2,"还车"),
        C(3,"维修"),
        D(4,"罚款");

        int id;
        String msg;

        event(int id,String s){
            this.id = id;
            msg = s;
        }

        public static int getId(String s){
            for(event tmp : event.values()){
                if(tmp.msg.equals(s))
                    return tmp.id;
            }
            return -1;
        }

        public static String getMsg(int id){
            for(event tmp : event.values()){
                if(tmp.id == id)
                    return tmp.msg;
            }
            return null;
        }

    }

    /**
     * 枚举报表类型
     */
    public enum timeType{
        A(1,"日报表","day"),
        B(2,"周报表","week"),
        C(3,"月报表","month"),
        D(4,"年报表","year");

        int id;
        String msg;
        String value;

        timeType(int id, String msg,String value) {
            this.id = id;
            this.msg = msg;
            this.value = value;
        }

        public static int getId(String s){
            for(timeType tmp : timeType.values()){
                if(tmp.msg.equals(s))
                    return tmp.id;
            }
            return -1;
        }

        public static String getTime(int i){
            for(timeType tmp : timeType.values()){
                if(tmp.id == i)
                    return tmp.value;
            }
            return null;
        }

        public static String getMsg(int i){
            for(timeType tmp : timeType.values()){
                if(tmp.id == i)
                    return tmp.msg;
            }
            return null;
        }
    }

    /**
     * 加载mysql驱动
     */
    private DBConnector(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("not found mysql driver");
            e.printStackTrace();
        }
    }

    /**
     * 线程安全的单例模式
     * @return 数据库连接实例
     */
    public static DBConnector getInstance(){
        if(instance == null){
            synchronized (DBConnector.class){
                if (instance == null)
                    instance = new DBConnector();
            }
        }
        return instance;
    }

    /**
     * 建立数据库连接
     * @param hostName 主机名
     * @param port 端口号
     * @param dbName 数据库名
     * @param user 用户名
     * @param passwd 密码
     */
    public void connectDB(String hostName,int port,String dbName,String user,String passwd){
        String url = "jdbc:mysql://"+hostName+":"+port+"/"+dbName+"?autoReconnect=true&characterEncoding=UTF-8&characterSetResults=UTF-8&serverTimezone=UTC";
        try {
            connection = DriverManager.getConnection(url,user,passwd);
            statement = connection.createStatement();
        } catch (SQLException throwables) {
            System.out.println("cannot connect to database");
            throwables.printStackTrace();
        }
    }

    /**
     * 获取顾客信息
     * @param brbh 顾客编号
     * @return 顾客信息
     */
    public ResultSet getCustomerInfo(String brbh) {
        try {
            return statement.executeQuery("select * from customerinfo where id = " + brbh);
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * 获取员工信息
     * @param brbh 员工编号
     * @return 员工信息
     */
    public ResultSet getEmployeeInfo(String brbh) {
        try {
            return statement.executeQuery("select * from employeeinfo where id =" + brbh);
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * 更新登录日期
     * @param loginIdenty 登录身份
     * @param user 用户
     */
    public void updateDLRQ(String loginIdenty,String user){
        try{
            String table = loginIdenty.equals("顾客")? "customerinfo" : "employeeinfo";
            statement.executeUpdate("update " + table + " set dlrq = DATE_FORMAT(NOW(),'%Y-%m-%d %H:%m:%s') where ID " + "=" + user);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 获取汽车品牌信息
     * @return 汽车品牌
     * @param pingyin 品牌拼音缩写
     */
    public ObservableList<String> getCarBrand(String pingyin){
        ObservableList<String> ans = FXCollections.observableArrayList();
        try{
            String pinyin;
            ResultSet resultSet = statement.executeQuery("select distinct brand , pyzs from carinfo");
            while (resultSet.next()){
                String tmp = resultSet.getString("brand");
                pinyin = resultSet.getString("pyzs");
                if(pingyin == null)
                    ans.add(tmp);
                else{
                    if(pinyin.contains(pingyin))
                        ans.add(tmp);
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return ans;
    }

    /**
     * 获取汽车租金情况
     * @param brand 品牌
     * @param status 状况
     * @return 汽车租金
     */
    public ObservableList<String> getCarPay(String brand,String status){
        ObservableList<String> ans = FXCollections.observableArrayList();
        try{
            String sql = "select distinct cost from carinfo where brand = '" + brand + "'";
            if(status != null)
                sql += " and status = " + status;
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()){
                String tmp = resultSet.getInt("cost") + " 元/天";
                ans.add(tmp);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return ans;
    }

    /**
     * 获取车辆状况
     * @param brand 车牌号
     * @param pay 租金
     * @return 车辆状况
     */
    public ObservableList<String> getCarStatus(String brand,String pay){
        ObservableList<String> ans = FXCollections.observableArrayList();
        try{
            String sql = "select distinct status from carinfo where brand = '" + brand + "'";
            if(pay != null)
                sql += " and cost = " + pay;
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()){
                ans.add(carStatus.getMsg(resultSet.getInt("status")));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return ans;
    }

    /**
     * 获取车牌信息
     * @param brand 车牌号
     * @param pay 租金
     * @param status 车况
     * @return 车牌号
     */
    public ObservableList<String> getCarId(String brand,String pay,String status){
        ObservableList<String> ans = FXCollections.observableArrayList();
        try{
            String sql = "select license from carinfo where valid = 1 and brand = '" + brand + "'";
            if(pay != null)
                sql += " and cost = " + pay;
            if(status != null)
                sql += " and status = " + status;
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()){
                ans.add(resultSet.getString("license"));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ans;
    }

    /**
     * 判断是否会员
     * @param id 用户ID
     * @return 是否会员
     */
    public boolean getMember(String id){
        boolean flag = false;
        try{
            ResultSet resultSet = statement.executeQuery("select member from customerinfo where id = " + id);
            while (resultSet.next())
                flag = resultSet.getInt("member") == 1 ;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return flag;
    }

    /**
     * 根据车牌号获取车辆信息
     * @param license 车牌号
     * @return 车辆信息
     * @throws SQLException yichang
     */
    public ResultSet getCarInfo(String license) throws SQLException {
        ResultSet resultSet = statement.executeQuery("select * from carinfo where license = '" + license + "'");
        return resultSet;
    }

    /**
     * 获取职工名称
     * @return 职工名称
     */
    public ObservableList<String> getEmployeeInfo(){
        ObservableList<String> ans = FXCollections.observableArrayList();
        try{
            ResultSet resultSet = statement.executeQuery("select name from employeeinfo");
            while (resultSet.next()) {
                ans.add(resultSet.getString("name"));
            }
            ans.remove("root");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return ans;
    }

    /**
     * 填日志
     * @param license 车牌
     * @param cusid 顾客
     * @param empid 员工
     * @param starttime 开始时间
     * @param edntime 结束时间
     * @param event 事件
     * @param profit 收益
     * @param detail 细节
     */
    public void setDiary(String license,String cusid,String empid,String starttime,String edntime,int event,int profit,String detail){
        String s = String.format("insert into diary(license,cusid,empid,starttime,endtime,event,profit,detail) values ('%s','%s','%s','%s','%s','%d','%d','%s')",
                license,cusid,empid,starttime,edntime,event,profit,detail);
        try{
            statement.executeUpdate(s);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 根据员工名字获取员工编号
     * @param name 员工名字
     * @return 员工编号
     */
    public int getEmployeeID(String name){
        int ans = -1;
        try{
            ResultSet resultSet = statement.executeQuery("select id from employeeinfo where name = '" + name + "'");
            while (resultSet.next())
                ans = resultSet.getInt("id");
        }catch (Exception e){
            e.printStackTrace();
        }
        return ans;
    }

    /**
     * 租车事务
     * @param license 车牌
     * @param cusid 顾客
     * @param empid 员工
     * @param starttime 开始时间
     * @param edntime 结束时间
     * @param event 事件
     * @param profit 收益
     * @param detail 细节
     * @return 成功与否
     */
    public boolean rentCarTranc(String license,String cusid,String empid,String starttime,String edntime,int event,int profit,String detail){
        try{
            boolean isValid = true;
            ResultSet resultSet = statement.executeQuery("select valid from carinfo where license = '" + license + "'");
            while (resultSet.next()){
                isValid = resultSet.getInt("valid") == 1;
            }
            if(!isValid)
                return  isValid;  //此时无效
            connection.setAutoCommit(false);   //开始事务
            statement.executeQuery("select * from carinfo where brand = '" + license + "' for update ");
            statement.executeUpdate("update carinfo set valid = 0 where license = '" + license + "'");
            setDiary(license,cusid,empid,starttime,edntime,event,profit,detail);
            connection.commit();
            connection.setAutoCommit(true);  //结束事务
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 获取租车信息
     * @param id 用户号
     * @return 租车信息
     */
    public ObservableList<customerController.rentCarInfo> getRentInfo(String id){
        ObservableList<customerController.rentCarInfo> ans = FXCollections.observableArrayList();
        try{
            String sql = "select diary.license,brand,status,peldge,endtime,profit from diary , carinfo where cusid = '" + id + "' AND diary.license = carinfo.license AND detail = '租车'";
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()){
                ans.add(new customerController.rentCarInfo(resultSet.getString("license"),resultSet.getString("brand"),resultSet.getInt("status"),resultSet.getInt("profit"),
                        resultSet.getInt("peldge"),resultSet.getString("endtime")));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return ans;
    }

    /**
     * 还车事件
     * @param id 用户id
     * @param license 车牌号
     * @return 返回逾期金额
     */
    public int returnCar(String id, String license){
        boolean flag = false;
        int profit = 0;
        try{
            connection.setAutoCommit(false);
            statement.executeQuery("select * from carinfo where license = '" + license + "' for update ");
            statement.executeUpdate("update carinfo set valid = 1 where license = '" + license + "'");
            statement.executeUpdate("update diary set detail = '租车已还车' where event = 1 and license = '" + license + "'");
            ResultSet resultSet = statement.executeQuery("select empid,endtime from diary where license = '" + license + "'" + "and event = 1 and cusid = " + id + " order by endtime desc");
            int empid = -1;
            LocalDateTime time = null;
            while (resultSet.next()){
                empid = resultSet.getInt("empid");
                time = LocalDateTime.parse(resultSet.getString("endtime"),DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            assert time != null;
            long minutes = Duration.between(time,LocalDateTime.now()).toMinutes();
            flag = minutes >= 0 ;  //逾期
            if(!flag)  //没有逾期
                setDiary(license,id,("" + empid), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),2,0,"还车");
            else{
                long days = Duration.between(time,LocalDateTime.now()).toDays() * 3 ;
                resultSet = statement.executeQuery("select peldge from carinfo where license = '" + license + "'");
                while (resultSet.next())
                    profit = resultSet.getInt("peldge");
                profit = (int) (days * profit);
                setDiary(license,id,("" + empid), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),2, profit ,"逾期");

            }
            connection.setAutoCommit(true);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            try{
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return profit;
    }

    /**
     * 获取所有车辆信息
     * @return 所有车辆信息
     */
    public ObservableList<employeeController.carInfo> getCarInfo(){
        ObservableList<employeeController.carInfo> ans = FXCollections.observableArrayList();
        try{
            ResultSet resultSet = statement.executeQuery("select * from carinfo");
            while (resultSet.next()){
                ans.add(new employeeController.carInfo(resultSet.getString("license"),resultSet.getString("brand"),resultSet.getInt("status"),
                        resultSet.getInt("cost"),resultSet.getInt("peldge"),resultSet.getInt("valid")));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return ans;
    }

    /**
     * 修改车辆信息
     * @param license 车牌
     * @param brand 品牌
     * @param cost 租金
     * @param status 状态
     * @param peldge 押金
     * @return 修改成功
     */
    public boolean modifyCar(String license,String brand,int cost,int status,int peldge){
        try{
            ResultSet resultSet = statement.executeQuery("select valid from carinfo where license = '" + license + "'");
            while (resultSet.next()){
                if(resultSet.getInt("valid") == 0)  //使用中
                    return false;
            }
            connection.setAutoCommit(false);
            statement.executeQuery("select * from carinfo where license = '" + license + "'for update ");
            statement.executeUpdate(String.format("update carinfo set brand = '%s',cost = '%d',status = '%d',peldge = '%d' where license = '%s'",brand,cost,status,peldge,license));
            connection.setAutoCommit(true);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }

    /**
     * 增加车辆
     * @param license 车牌
     * @param brand 品牌
     * @param cost 租金
     * @param status 状态
     * @param peldge 押金
     * @return 增加成功
     */
    public boolean addCar(String license,String brand,int cost,int status,int peldge){
        try{
            ResultSet resultSet = statement.executeQuery("select license from carinfo where license = '" + license + "'");
            if(resultSet.next())
                return false;
            String pinyin = "";
            resultSet = statement.executeQuery("select distinct pyzs from carinfo where brand = '" + brand + "'");
            while (resultSet.next())
                pinyin = resultSet.getString("pyzs");
            statement.executeUpdate(String.format("insert into carinfo (license,brand,cost,status,peldge,pyzs,valid) values ('%s','%s',%d,%d,%d,'%s',1)",license,brand,cost,status,peldge,pinyin));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return true;
    }

    /**
     * 删除车辆
     * @param license 车牌
     * @return 成功与否
     */
    public boolean deleteCar(String license){
        boolean flag = true;
        try {
            ResultSet resultSet = statement.executeQuery("select valid from  carinfo where license = '" + license + "'");
            while (resultSet.next())
                flag = resultSet.getInt("valid") == 1;
            if(!flag)
                return flag;
            connection.setAutoCommit(false);
            statement.executeQuery("select  * from carinfo where license = '" + license + "'");
            statement.executeUpdate("delete from carinfo where license = '" + license + "'");
            connection.setAutoCommit(true);
        }catch (Exception e){
            e.printStackTrace();
            try {
                connection.setAutoCommit(true);
                connection.rollback();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return false;
        }
        return flag;
    }

    /**
     * 获取顾客信息
     * @return 获取所有顾客信息
     */
    public ObservableList<employeeController.cusInfo> getCusInfo(){
        ObservableList<employeeController.cusInfo> ans = FXCollections.observableArrayList();
        try{
            ResultSet resultSet = statement.executeQuery("select customerinfo.id,name,passwd,member,dlrq,ifnull(tmp.grade,0) as grade from customerinfo " +
                    "left join  (select cusid,count(event) as grade from diary where (event = 3 OR event = 4 OR detail = '逾期') group by cusid ) tmp " +
                    "on customerinfo.id = tmp.cusid ");
            while (resultSet.next()){
                ans.add(new employeeController.cusInfo(resultSet.getInt("id"),resultSet.getString("name"),resultSet.getInt("member"),
                        resultSet.getInt("grade"),resultSet.getString("passwd"),resultSet.getString("dlrq")));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return ans;
    }

    /**
     * 修改用户信息
     * @param id 账号
     * @param name 名
     * @param passwd 密码
     * @param member 会员
     * @return 修改结果
     */
    public boolean modifyCus(String id,String name,String passwd,String member){
        try{
            ResultSet resultSet = statement.executeQuery("select id from customerinfo where id = " + id);
            if(!resultSet.next())  //先确认用户存在
                return false;
            statement.executeUpdate(String.format("update customerinfo set name = '%s',passwd = '%s', member = '%s' where id = '%s' ",name,passwd,member.equals("会员") ? 1 : 0,id));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return true;
    }

    /**
     * 增加新用户
     * @param id 账号
     * @param name 名
     * @param passwd 密码
     * @param member 会员
     * @return 增加结果
     */
    public boolean addNewCus(String id,String name,String passwd,String member){
        try{
            ResultSet resultSet = statement.executeQuery("select id from customerinfo where id = " + id);
            if(resultSet.next())
                return false;
            statement.executeUpdate(String.format("insert into customerinfo(id,name,passwd,member,dlrq) values ('%s','%s','%s',%d,'%s')",
                    id,name,passwd,member.equals("会员") ? 1 : 0,LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"))));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return true;
    }

    /**
     * 删除用户
     * @param id 用户名
     * @return 结果
     */
    public boolean deleteCus(String id){
        try{
            ResultSet resultSet = statement.executeQuery("select count(id) as cnt from diary where detail = '租车' and cusid = '" + id + "'");
            while (resultSet.next()){
                if(resultSet.getInt("cnt") > 0)
                    return false;
            }
            statement.executeUpdate("delete from customerinfo where id = '" + id + "'");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return true;
    }

    /**
     * 根据员工权限浏览对应的信息
     * @param userId 员工id
     * @param title 员工职称
     * @return 员工信息
     */
    public ObservableList<employeeController.employeeInfo> getEmpInfo(String userId,int title){
        ObservableList<employeeController.employeeInfo> ans = FXCollections.observableArrayList();
        try{
            ResultSet resultSet = statement.executeQuery(String.format("select * from employeeinfo where (title < %d or id = %s)",title,userId));
            while (resultSet.next())
                ans.add(new employeeController.employeeInfo(resultSet.getInt("id"),resultSet.getString("name"),resultSet.getInt("title"),
                        resultSet.getString("passwd"),resultSet.getString("dlrq")));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return ans;
    }

    /**
     * 修改员工信息
     * @param id id
     * @param name 姓名
     * @param title 职称
     * @param passWd 密码
     * @param userTitle 操作员的职称
     * @return 状态码
     */
    public int modifyEmp(String id,String name,String title,String passWd,int userTitle ){
        try{
            int needTitle = DBConnector.title.getTitle(title);
            if(needTitle == -1)
                return -2;  //输入的职称无效
            if(needTitle >= userTitle)
                return 0;  //不能修改成和自己同级或者比自己高级的职称
            ResultSet resultSet = statement.executeQuery("select id from employeeinfo where id = " + id);
            if (!resultSet.next())
                return -1;  //不存在该员工
            statement.executeUpdate(String.format("update employeeinfo set name = '%s',title = '%d', passwd = '%s' where id = '%s'",name,needTitle,passWd,id));
            return 1;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return -10;
        }
    }

    /**
     * 增加员工信息
     * @param id id
     * @param name 姓名
     * @param title 职称
     * @param passWd 密码
     * @param userTitle 操作员的职称
     * @return 状态码
     */
    public int addEmp(String id,String name,String title,String passWd,int userTitle ){
        try{
            int needTitle = DBConnector.title.getTitle(title);
            if(needTitle == -1)
                return -2;  //输入的职称无效
            if(needTitle >= userTitle)
                return 0;  //不能修改成和自己同级或者比自己高级的职称
            ResultSet resultSet = statement.executeQuery("select id from employeeinfo where id = " + id);
            if (resultSet.next())
                return -1;  //存在该员工
            statement.executeUpdate(String.format("insert into employeeinfo values('%s','%s','%s','%d','%s')",id,name,passWd,
                    needTitle,LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"))));
            return 1;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return -10;
        }
    }

    /**
     * 删除员工信息
     * @param id id
     * @param title 职称
     * @param userTitle 操作员职称
     * @return 状态码
     */
    public int deleteEmp(String id,String title,int userTitle){
        try{
            int needTitle = DBConnector.title.getTitle(title);
            if(needTitle == -1)
                return -2;  //输入的职称无效
            if(needTitle >= userTitle)
                return 0;  //不能修改成和自己同级或者比自己高级的职称
            ResultSet resultSet = statement.executeQuery("select id from employeeinfo where id = " + id);
            if (!resultSet.next())
                return -1;  //不存在该员工
            statement.executeUpdate("delete from employeeinfo where id = " + id);
            return 1;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return -10;
        }
    }

    /**
     * 根据权限查看日志
     * @param userId 用户id
     * @param title 职称
     * @return 日志信息
     */
    public ObservableList<employeeController.diaryInfo> getDiaInfo(String userId,int title){
        ObservableList<employeeController.diaryInfo> ans = FXCollections.observableArrayList();
        try{
            ResultSet resultSet = statement.executeQuery(String.format("select diary.id, diary.license,customerinfo.name as cus,employeeinfo.name as ename,starttime,endtime," +
                    "event,profit,detail from diary,employeeinfo,customerinfo where diary.cusid = customerinfo.id and diary.empid = employeeinfo.id and " +
                    "empid in (select empid from diary where (title < %d or empid = %s))",title,userId));
            while (resultSet.next()){
                ans.add(new employeeController.diaryInfo(resultSet.getString("id"),resultSet.getString("license"),resultSet.getString("cus"),
                        resultSet.getString("ename"),resultSet.getString("starttime"),resultSet.getString("endtime"),
                        resultSet.getInt("event"),resultSet.getInt("profit"),resultSet.getString("detail")));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return ans;
    }

    /**
     * 删除日志
     * @param id 日志编号
     * @return 成功与否
     */
    public boolean deleteDiary(String id){
        try{
            statement.executeUpdate("delete from diary where id = " + id);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 根据用户名获取编号
     * @param name 用户名
     * @return 用户编号
     */
    public int getCusId(String name){
        int ans = -1;
        try {
            ResultSet resultSet = statement.executeQuery("select id from customerinfo where name = '" + name + "'");
            while (resultSet.next())
                ans = resultSet.getInt("id");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return ans;
    }

    /**
     * 个人报表信息
     * @param userId 用户ID
     * @param type 时间段
     * @param sTime 开始时间
     * @param eTime 结束时间
     * @return 年收入
     */
    public XYChart.Series<String,Number> getPersonChart(String userId, int type, String sTime, String eTime){
        XYChart.Series<String,Number> ans = new XYChart.Series<>();
        ans.setName("当"+timeType.getMsg(type).substring(0,1)+"收入");
        try{
            ResultSet resultSet = statement.executeQuery(String.format("select left(starttime,%d) as mon ,sum(profit) as sum from diary where empid = %s " +
                    "and starttime >= '%s' and starttime <= '%s' group by %s(starttime) order by mon",(type <= 2 ? 10 : type == 3 ? 7 : 4 ),
                    userId,sTime,eTime,timeType.getTime(type)));
            while (resultSet.next()){
                ans.getData().add(new XYChart.Data<>((resultSet.getString("mon") + (type == 4 ? " 年" : type == 3 ? " 月" : " 日")),
                        resultSet.getInt("sum")));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return ans;
    }

    /**
     * 获取所有车辆收入信息
     * @param type 时间
     * @param start 开始时间
     * @param end 结束时间
     * @return 车辆收入信息
     */
    public LinkedList<XYChart.Series<String ,Number>> getCarChart(int type, String start, String end){
        LinkedList<XYChart.Series<String ,Number>> ans = new LinkedList<>();
        try{
            Statement st = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select distinct brand from carinfo");
            while (resultSet.next()){
                String tmp = resultSet.getString("brand");
                ResultSet res = st.executeQuery(String.format("select sum(profit) as sum , left(starttime,%d) as mon from diary where starttime >= '%s' " +
                                "and starttime <= '%s' and license in (select license from carinfo where brand = '%s') group by %s(starttime) order by mon",
                        (type <= 2 ? 10 : type == 3 ? 7 : 4),start,end, tmp,timeType.getTime(type)));
                XYChart.Series<String,Number> temp = new XYChart.Series<>();
                temp.setName(tmp + timeType.getMsg(type).substring(0,1) + "收入");
                while (res.next()){
                    temp.getData().add(new XYChart.Data<>((res.getString("mon") + (type == 4 ? " 年" : type == 3 ? " 月" : " 日")),
                            res.getInt("sum")));
                }
                if(temp.getData().size() > 0)
                    ans.add(temp);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ans;
    }

    /**
     * 年报表
     * @param type 时间段
     * @param start 开始时间
     * @param end 结束时间
     * @return 年收入
     */
    public XYChart.Series<String,Number> getYearChart(int type, String start, String end){
        XYChart.Series<String,Number> ans = new XYChart.Series<>();
        ans.setName("公司当"+timeType.getMsg(type).substring(0,1)+"收入");
        try{
            ResultSet resultSet = statement.executeQuery(String.format("select left(starttime,%d) as mon ,sum(profit) as sum from diary where starttime >= '%s' " +
                    "and starttime <= '%s'group by %s(starttime) order by mon",(type <= 2 ? 10 : type == 3 ? 7 : 4),start,end,timeType.getTime(type)));
            while (resultSet.next()){
                ans.getData().add(new XYChart.Data<>((resultSet.getString("mon") + (type == 4 ? " 年" : type == 3 ? " 月" : " 日")),
                        resultSet.getInt("sum")));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return ans;
    }

    /**
     * 填充初始日志表
     */
    public void addDiary(){
        String time ="2020-01-01 14:00:00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss" );
        LocalDateTime dateTime = LocalDateTime.parse(time,formatter);
        LinkedList<String> brands = new LinkedList<>();
        Random random = new Random();
        try{
            ResultSet resultSet = statement.executeQuery("select distinct brand from carinfo");
            while (resultSet.next())
                brands.add(resultSet.getString("brand"));
            resultSet.close();
            LinkedList<LinkedList<String>> licenses = new LinkedList<>();
            for (String brand : brands) {
                resultSet = statement.executeQuery("select license from carinfo where brand = '" + brand + "'");
                LinkedList<String> tmp = new LinkedList<>();
                while (resultSet.next()) {
                    tmp.add(resultSet.getString("license"));
                }
                resultSet.close();
                if (tmp.size() > 0)
                    licenses.add(tmp);
            }
            resultSet.close();
            LinkedList<String> cusids = new LinkedList<>();
            resultSet = statement.executeQuery("select id from customerinfo");
            while (resultSet.next())
                cusids.add(resultSet.getString("id"));
            resultSet.close();
            LinkedList<String> empids = new LinkedList<>();
            resultSet = statement.executeQuery("select id from employeeinfo");
            String s = " ";
            while (resultSet.next())
                empids.add(resultSet.getString("id"));
            for(int i = 0 ; i < 214 ; ++i ){
                for(LinkedList<String> license : licenses){
                    int size = random.nextInt(license.size()) + 1;
                    for(int j = 0 ; j < size ; ++j){
                        String tmp_licnese = license.get(random.nextInt(license.size()));
                        int profit = (random.nextInt(91) + 10) * 100;
                        profit /= 2;
                        int event = random.nextInt(4) + 1;
                        String cusid = cusids.get(random.nextInt(cusids.size()));
                        String empid = empids.get(random.nextInt(empids.size()));
                        setDiary(tmp_licnese,cusid,empid,time,time,event,profit,s);
                        //System.out.println(String.format("填日志： %s %s %s %d %d %s",tmp_licnese,cusid,empid,event,profit,time));
                    }
                }
                dateTime = dateTime.minusDays(-1);
                time = dateTime.format(formatter);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 生成车牌号
     */
    public void addCar(){
        Random random = new Random();
        String id = "鄂A ";
        String alphas = "QWERTYUIOPASDFGHJKLZXCVBNM"; // 26个字母 + 10个数字
        try{
            ResultSet resultSet = statement.executeQuery("select distinct brand from carinfo");
            LinkedList<String> ans = new LinkedList<>();
            while (resultSet.next())
                ans.add(resultSet.getString("brand"));
            for(String brand : ans){
                for(int i = 0 ; i < 10 ; ++i){
                    int cost = (random.nextInt(88) + 10) * 100;
                    int ya = cost + (random.nextInt(31) + 10) * 100;
                    int status = random.nextInt(5) + 1;
                    String sb = id + random.nextInt(10) +
                            alphas.charAt(random.nextInt(26)) +
                            random.nextInt(10) +
                            alphas.charAt(random.nextInt(26)) +
                            random.nextInt(10);
                    addCar(sb,brand,cost,status,ya);
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        System.out.println("增加车辆完成");
    }

    /**
     * 增加新用户
     */
    public void addMember(){
        Random random = new Random();
        for(int i = 0 ; i < 1000 ; ++i){
            String name = Util.getChineseName();
            int id = random.nextInt(1000) + random.nextInt(10) * 1000 + 1;
            String member = random.nextBoolean() ? "会员" : "非会员";
            String dlrq = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));
            addNewCus(("" + id),name,"123456",member);
        }
        System.out.println("增加新用户完成");
    }

    /**
     * 增加新员工
     */
    public void addEmployee(){
        Random random = new Random();
        for(int i = 0 ; i < 10 ; ++i){
            String name = Util.getChineseName();
            int id = random.nextInt(100) + random.nextInt(10) * 100 + 1;
            String dlrq = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));
            int title = random.nextInt(3) + 1;
            addEmp((id + ""),name,DBConnector.title.getName(title),"123456",2147483647);
        }
        System.out.println("增加新员工完成");
    }

    public static void main (String[] args){
        DBConnector.getInstance().connectDB("localhost",3306,"dblab3","root","root");
        /*DBConnector.getInstance().addCar();
        DBConnector.getInstance().addMember();
        DBConnector.getInstance().addEmployee();
        DBConnector.getInstance().addDiary();*/
    }

}
