package cn.edu.sustech.cs209.chatclient.controller;

import cn.edu.sustech.cs209.chatclient.model.User;
import cn.edu.sustech.cs209.chatclient.model.UserPI;
import cn.edu.sustech.cs209.chatclient.net.ClientConnector;
import cn.edu.sustech.cs209.chatclient.packet.PacketType;
import cn.edu.sustech.cs209.chatclient.view.ChatPane;
import cn.edu.sustech.cs209.chatclient.packet.Packet;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ChatController {
	
	private LoginController loginController;
	private User user;
	private List<UserPI> userPIs;
	private ChatPane pane;
	private ClientConnector connector;
	private ClientThread thread;
	public ChatController(LoginController loginController, ClientConnector connector) {
		this.loginController = loginController;
		this.connector = connector;
	}
	
	protected void startClientThread() {
		this.thread = new ClientThread(this, this.connector);
		this.thread.setDaemon(true);
		this.thread.start();
	}
	
	public void handlePacket(Packet packet) {
		//Notice JFX thread
		if (packet.getType() == PacketType.USER) {
			if (packet.getSubCode() == 0) {
				JSONArray array = packet.getContent().getJSONArray("users");
				JSONObject[] userArray = new JSONObject[array.size()];
				array.toArray(userArray);
				this.userPIs = new ArrayList<>();
				for (JSONObject object : userArray) {
					UserPI userPI = (UserPI) JSON.parseObject(object.toJSONString(), UserPI.class);
					this.userPIs.add(userPI);
				}
			}
		}
	}
	
	public User getUser() {
		return this.user;
	}
	
	public void showChatPane() {
		this.pane = new ChatPane(this);
		this.pane.init();
		this.pane.show();
		this.loginController.hideLoginPane();
	}
	
}
