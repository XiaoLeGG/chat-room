package cn.edu.sustech.cs209.chatclient.controller;

import cn.edu.sustech.cs209.chatclient.model.User;
import cn.edu.sustech.cs209.chatclient.net.ClientConnector;
import cn.edu.sustech.cs209.chatclient.view.LoginPane;
import cn.edu.sustech.cs209.chatclient.packet.Packet;
import com.alibaba.fastjson.JSON;
import javafx.stage.Stage;

public class LoginController {
	
	private LoginPane pane;
	private ChatController chatController;
	
	public void initLogin() {
		this.pane = new LoginPane(this);
	}
	
	public void showLoginPane(Stage stage) {
		this.pane.init(stage);
		this.pane.show();
	}
	
	public String register(String username, String password) {
		ClientConnector connector = new ClientConnector(username, password);
		Packet packet = connector.register();
		if (packet == null) {
			return "连接到服务器失败";
		} else {
			if (packet.getSubCode() == 0) {
				return "注册失败";
			}
			if (packet.getSubCode() == 1) {
				return packet.getContent().getString("msg");
			} else {
				return "注册成功";
			}
		}
	}
	
	public void hideLoginPane() {
		this.pane.hide();
	}
	
	public String login(String username, String password) {
		ClientConnector connector = new ClientConnector(username, password);
		Packet packet = connector.login();
		if (packet != null) {
			if (packet.getSubCode() == 0) {
				return "登录失败，请检查用户名和密码";
			}
			this.chatController = new ChatController(this, connector, JSON.parseObject(packet.getContent().toJSONString(), User.class));
			this.chatController.showChatPane();
			this.chatController.startClientThread();
			return "登录成功";
		} else {
			return "连接到服务器失败";
		}
	}
	
	
}
