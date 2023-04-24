package cn.edu.sustech.cs209.chatclient;

import cn.edu.sustech.cs209.chatclient.controller.LoginController;
import javafx.application.Application;
import javafx.stage.Stage;


public class MainApplication extends Application {
	
	@Override
	public void start(Stage stage) {
		LoginController controller = new LoginController();
		controller.initLogin();
		controller.showLoginPane(stage);
	}
	
	public static void main(String[] args) {
		try {
			Class.forName("org.burningwave.core.assembler.StaticComponentContainer");
		} catch(Throwable e) {
			if (debug()) {
				e.printStackTrace();
			}
		}
		launch();
	}
	
	public static boolean debug() {
		return true;
	}
	
}