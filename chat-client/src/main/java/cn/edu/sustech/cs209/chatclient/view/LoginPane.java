package cn.edu.sustech.cs209.chatclient.view;

import cn.edu.sustech.cs209.chatclient.controller.LoginController;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.effects.JFXDepthManager;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.jfoenix.controls.JFXButton;

public class LoginPane {
	
	private StackPane stackPane;
	private Stage stage;
	private Scene scene;
	private Pane loginPane;
	private Pane registerPane;
	private LoginController controller;
	
	public LoginPane(LoginController controller) {
		this.controller = controller;
	}
	
	public void init(Stage stage) {
		this.stackPane = new StackPane();
		this.scene = new Scene(stackPane);
		this.stage = stage;
		this.stage.setScene(scene);
		this.stackPane.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
		this.stackPane.getStyleClass().add("main-background");
		this.stage.setTitle("Chat Login");
		this.loginPane = this.createLoginPane();
		this.stackPane.getChildren().add(this.loginPane);
		StackPane.setAlignment(loginPane, Pos.CENTER);
	}
	
	public void hide() {
		this.stage.hide();
	}
	
	private Pane createLoginPane() {
		StackPane pane = new StackPane();
		pane.getStyleClass().add("login-background");
		VBox vbox = new VBox();
		vbox.setAlignment(Pos.CENTER);
		StackPane.setAlignment(vbox, Pos.CENTER);
		vbox.setSpacing(40);
		
		Label title = new Label("Custom Chat");
		title.getStyleClass().add("main-title");
		JFXDepthManager.setDepth(title, 1);
		
		HBox userNameBox = new HBox();
		JFXTextField textField = new JFXTextField("");
		Label user = new Label("用户名");
		user.getStyleClass().add("main-label");
		userNameBox.getChildren().addAll(user, textField);
		userNameBox.setAlignment(Pos.CENTER);
		userNameBox.setSpacing(10);
		JFXDepthManager.setDepth(user, 1);
		
		HBox passwordBox = new HBox();
		JFXPasswordField passwordField = new JFXPasswordField();
		Label pass = new Label("密码");
		pass.getStyleClass().add("main-label");
		passwordBox.getChildren().addAll(pass, passwordField);
		passwordBox.setAlignment(Pos.CENTER);
		passwordBox.setSpacing(10);
		JFXDepthManager.setDepth(pass, 1);
		
		HBox loginAndRegister = new HBox();
		loginAndRegister.setAlignment(Pos.CENTER);
		loginAndRegister.setSpacing(10);
		JFXButton login = new JFXButton("登录");
		login.getStyleClass().add("main-button");
		JFXDepthManager.setDepth(login, 2);
		login.setOnAction(event -> {
			if (textField.getText().isEmpty() || passwordField.getText().isEmpty()) {
				ViewUtils.showWarning(pane, ViewUtils.generateTextFlow("用户名或密码不能为空"));
				return;
			}
			String msg = this.controller.login(textField.getText(), passwordField.getText());
			if (!msg.equals("登录成功")) {
				ViewUtils.showWarning(pane, ViewUtils.generateTextFlow(msg));
			}
		});
		
		JFXButton register = new JFXButton("注册");
		register.getStyleClass().add("main-button");
		JFXDepthManager.setDepth(register, 2);
		loginAndRegister.getChildren().addAll(login, register);
		register.onActionProperty().set(event -> {
			this.initRegister();
		});
		
		vbox.getChildren().addAll(title, userNameBox, passwordBox, loginAndRegister);
		pane.getChildren().add(vbox);
		JFXDepthManager.setDepth(pane, 2);
		return pane;
	}
	
	public void initRegister() {
		this.stackPane.getChildren().remove(this.loginPane);
		this.registerPane = this.createRegisterPane();
		this.stackPane.getChildren().add(this.registerPane);
	}
	
	public void initLogin() {
		this.stackPane.getChildren().remove(this.registerPane);
		this.loginPane = this.createLoginPane();
		this.stackPane.getChildren().add(this.loginPane);
	}
	
	public Pane createRegisterPane() {
		StackPane pane = new StackPane();
		pane.getStyleClass().add("login-background");
		VBox vbox = new VBox();
		vbox.setAlignment(Pos.CENTER);
		StackPane.setAlignment(vbox, Pos.CENTER);
		vbox.setSpacing(40);
		
		Label title = new Label("Custom Chat");
		title.getStyleClass().add("main-title");
		JFXDepthManager.setDepth(title, 1);
		
		HBox userNameBox = new HBox();
		JFXTextField textField = new JFXTextField("");
		Label user = new Label("用户名");
		user.getStyleClass().add("main-label");
		userNameBox.getChildren().addAll(user, textField);
		userNameBox.setAlignment(Pos.CENTER);
		userNameBox.setSpacing(10);
		JFXDepthManager.setDepth(user, 1);
		
		HBox passwordBox = new HBox();
		JFXPasswordField passwordField = new JFXPasswordField();
		Label pass = new Label("密码");
		pass.getStyleClass().add("main-label");
		passwordBox.getChildren().addAll(pass, passwordField);
		passwordBox.setAlignment(Pos.CENTER);
		passwordBox.setSpacing(10);
		JFXDepthManager.setDepth(pass, 1);
		
		HBox confirmPasswordBox = new HBox();
		JFXPasswordField confirmPasswordField = new JFXPasswordField();
		Label confirmPass = new Label("确认密码");
		confirmPass.getStyleClass().add("main-label");
		confirmPasswordBox.getChildren().addAll(confirmPass, confirmPasswordField);
		confirmPasswordBox.setAlignment(Pos.CENTER);
		confirmPasswordBox.setSpacing(10);
		JFXDepthManager.setDepth(confirmPass, 1);
		
		JFXButton registerButton = new JFXButton("注册");
		registerButton.getStyleClass().add("main-button");
		JFXDepthManager.setDepth(registerButton, 2);
		registerButton.onActionProperty().set(event -> {
			if (passwordField.getText().isEmpty()) {
				ViewUtils.showWarning(pane, ViewUtils.generateTextFlow("密码不能为空"));
				return;
			}
			if (passwordField.getText().equals(confirmPasswordField.getText())) {
				String msg = this.controller.register(textField.getText(), passwordField.getText());
				if (msg.equals("注册成功")) {
					this.initLogin();
					ViewUtils.showInfomation((StackPane) this.loginPane, ViewUtils.generateTextFlow(msg));
				} else {
					ViewUtils.showWarning(pane, ViewUtils.generateTextFlow(msg));
				}
			} else {
				ViewUtils.showWarning(pane, ViewUtils.generateTextFlow("两次输入的密码不一致"));
			}
		});
		
		vbox.getChildren().addAll(title, userNameBox, passwordBox, confirmPasswordBox, registerButton);
		pane.getChildren().add(vbox);
		JFXDepthManager.setDepth(pane, 2);
		return pane;
	}
	
	public void show() {
		this.stage.show();
	}
	
}
