package cn.edu.sustech.cs209.chatclient.controller;

import cn.edu.sustech.cs209.chatclient.net.ClientConnector;
import cn.edu.sustech.cs209.chatclient.view.ChatPane;
import cn.edu.sustech.cs209.chatclient.packet.Packet;

public class ChatController {
	
	private LoginController loginController;
	
	private ChatPane pane;
	public ChatController(LoginController loginController, ClientConnector connector) {
		this.loginController = loginController;
	}
	
	public void handlePacket(Packet packet) {
		//Notice JFX thread
	}
	
	public void showChatPane() {
		this.pane = new ChatPane(this);
		this.pane.init();
		this.pane.show();
		this.loginController.hideLoginPane();
	}
	
}
