package chapter02;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

public class win extends Application {

    private Button btnExit = new Button("退出");
    private Button btnSend = new Button("发送");
    private Button btnOpen = new Button("加载");
    private Button btnSave = new Button("保存");
    //待发送信息的文本框
    private TextField tfSend = new TextField();
    //显示信息的文本区域
    private TextArea taDisplay = new TextArea();
    private TextField ipInput = new TextField("127.0.0.1");
    private TextField portInput = new TextField("8008");
    private Button btnConn = new Button("连接");
    private String ip;
    private String port;
    private TCPClient tcpClient;

    public static void main(String[] args) {
        launch(args);
    }

    public void sendMsgToTaDisplay(String msg) {
        if (!"".equals(msg)) {
            taDisplay.appendText(msg + "\n");
            tfSend.clear();
        }
    }
    public void exitSystem(){
        if(tcpClient != null){
            //向服务器发送关闭连接的约定信息
            tcpClient.send("bye");
            try {
                taDisplay.appendText(tcpClient.receive() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
            tcpClient.close();
        }
        System.exit(0);
    }
    public void sendMsgToServer(String msg) throws IOException {
        if (!"".equals(msg) && tcpClient != null) {
            tcpClient.send(msg);
            taDisplay.appendText(tcpClient.receive() + "\n");
        }

    }

    @Override
    public void start(Stage primaryStage) {

        // 初始化TextFileIO类
        TextFileIO textFileIO = new TextFileIO();
        // 设置发送框为焦点
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                // 该文本框在界面初始化时获取焦点
                tfSend.requestFocus();
                taDisplay.setEditable(false);
                taDisplay.setWrapText(true);
            }
        });
        // 退出按钮时间
        btnExit.setOnAction(event -> {
            exitSystem();
        });
        // 窗口右上角退出
        primaryStage.setOnCloseRequest(event -> {
            exitSystem();
        });
        // 发送按钮事件
        btnSend.setOnAction(event -> {
            String msg = tfSend.getText();
            sendMsgToTaDisplay(msg);
            try {
                sendMsgToServer(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        // 回车绑定事件
        tfSend.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                // 检测文本框是否为空，为空的话就不要发了
                String mess = tfSend.getText().trim();
                if (!"".equals(mess)) {
                    event.consume(); // otherwise a new line will be added to the textArea after the sendFunction() call
                    String msg;
                    if (event.isShiftDown()) {
                        msg = "echo: " + tfSend.getText();
                    } else {
                        msg = tfSend.getText();
                    }
                    sendMsgToTaDisplay(msg);
                    try {
                        sendMsgToServer(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        // 保存按钮事件
        btnSave.setOnAction(event -> {
            //"2:3:4:5".split(":")//将返回["2", "3", "4", "5"] 主要是解决getText不保存\n
            String[] textArray = taDisplay.getText().split("\n");
            StringBuilder text = new StringBuilder();
            text.append("\r\n");
            for (String value : textArray) {
                text.append(value).append("\r\n");
            }
            String s = text.toString();
            // 选择文件窗口
            FileChooser fileChooser = new FileChooser();
            // 保存选择的文件到file
            File file = fileChooser.showSaveDialog(null);
            //添加当前时间信息进行保存
            textFileIO.append(
                    LocalDateTime.now().withNano(0) + " " + s, file);

        });
        // 加载按钮事件
        btnOpen.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(null);
            String msg = textFileIO.load(file);
            System.out.println("output msg: " + msg);
            if (msg != null) {
                taDisplay.clear();
                taDisplay.setText(msg);
            }
        });
        // 连接按钮事件
        btnConn.setOnAction(event -> {
            this.ip = ipInput.getText();
            this.port = portInput.getText();
            if (ip.equals("") || port.equals("")) {
                taDisplay.appendText("ip或者port不能为空\n");
            } else {
                try {
                    this.tcpClient = new TCPClient(ip, port);
                    taDisplay.appendText(tcpClient.receive() + "\n");
                } catch (IOException e) {
                    taDisplay.appendText("连接失败\n");
                }
            }

        });
        //内容显示区域
        BorderPane mainPane = new BorderPane();
        // ip连接显示控件
        HBox topHBox = new HBox();
        topHBox.setSpacing(10);//各控件之间的间隔
        //VBox面板中的内容距离四周的留空区域 内距
        topHBox.setPadding(new Insets(10, 20, 10, 20));
        topHBox.getChildren().addAll(new Label("IP地址："),
                ipInput, new Label("端口号："), portInput, btnConn);
        VBox vBox = new VBox();
        vBox.setSpacing(10);//各控件之间的间隔
        //VBox面板中的内容距离四周的留空区域 内距
        vBox.setPadding(new Insets(10, 20, 10, 20));
        vBox.getChildren().addAll(topHBox, new Label("信息显示区："),
                taDisplay, new Label("信息输入区："), tfSend);
        //设置显示信息区的文本区域可以纵向自动扩充范围
        VBox.setVgrow(taDisplay, Priority.ALWAYS);

        mainPane.setCenter(vBox);
        //底部按钮区域
        HBox hBox = new HBox();
        hBox.setSpacing(10);
        hBox.setPadding(new Insets(10, 20, 10, 20));
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.getChildren().addAll(btnSend, btnSave, btnOpen, btnExit);
        mainPane.setBottom(hBox);
        Scene scene = new Scene(mainPane, 700, 400);
        primaryStage.setScene(scene);
        primaryStage.show();

    }
}
