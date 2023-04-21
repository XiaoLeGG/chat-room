package cn.edu.sustech.cs209.chatclient.net;

import cn.edu.sustech.cs209.chatclient.model.User;
import cn.edu.sustech.cs209.chatclient.model.UserManager;
import cn.edu.sustech.cs209.chatclient.packet.PacketIO;
import cn.edu.sustech.cs209.chatclient.packet.Packet;
import cn.edu.sustech.cs209.chatclient.packet.PacketType;
import cn.edu.sustech.cs209.chatclient.packet.PacketWrapper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class UserThread extends Thread {
	
	private Socket socket;
	private ChatServer server;
	private User user;
	
	public UserThread(Socket socket, ChatServer server) {
		this.socket = socket;
		this.server = server;
	}
	
	@Override
	public void run() {
		try {
			Packet packet = PacketIO.receivePacket(this.socket.getInputStream());
			if (packet == null) {
				this.socket.close();
				return;
			}
			if (packet.getType() == PacketType.LOGIN) {
				this.server.info("Receive login request from " + socket.getInetAddress());
				JSONObject content = packet.getContent();
				if (!content.containsKey("username") || !content.containsKey("password")) {
					this.socket.close();
					return;
				}
				String username = content.getString("username");
				String password = content.getString("password");
				this.user = this.server.getUserManager().loginUser(username, password, this.socket);
				if (user == null) {
					PacketIO.sendPacket(this.socket.getOutputStream(), new Packet(PacketType.LOGIN, "server", 0, 0, new JSONObject()));
					this.socket.close();
					return;
				} else {
					PacketIO.sendPacket(this.socket.getOutputStream(), new Packet(PacketType.LOGIN, "server", 0, 1, (JSONObject) JSON.toJSON(user)));
					JSONArray array = (JSONArray) JSON.toJSON(this.server.getUserManager().getUserPIs());
					JSONObject userContent = new JSONObject();
					userContent.put("users", array);
					System.out.println(array.toJSONString());
					PacketIO.sendPacket(this.socket.getOutputStream(), new Packet(PacketType.USER, "server", 0, 0, userContent));
					this.startReceive();
				}
			}
			if (packet.getType() == PacketType.REGISTER) {
				this.server.info("Receive register request from " + socket.getInetAddress());
				JSONObject content = packet.getContent();
				if (!content.containsKey("username") || !content.containsKey("password")) {
					return;
				}
				String username = content.getString("username");
				String password = content.getString("password");
				User user = this.server.getUserManager().createUser(username, password);
				if (user == null) {
					PacketIO.sendPacket(this.socket.getOutputStream(),
						new Packet(PacketType.REGISTER, "server", 0, 0, new JSONObject()));
					this.socket.close();
					return;
				}
				if (user.getUserName() == null) {
					PacketIO.sendPacket(this.socket.getOutputStream(),
						new Packet(PacketType.REGISTER, "server", 0, 1, JSON.parseObject("{\"msg\": \"用户名已存在\"}")));
					this.socket.close();
					return;
				}
				PacketIO.sendPacket(this.socket.getOutputStream(),
					new Packet(PacketType.REGISTER, "server", 0, 2, (JSONObject) JSON.toJSON(user)));
				this.socket.close();
			}
			return;
		} catch (Exception e) {
			if (this.server.debug()) {
				e.printStackTrace();
			}
			return;
		}
	}
	
	private void startReceive() {
		try {
			InputStream input = this.socket.getInputStream();
			while (true) {
				Packet packet = PacketIO.receivePacket(input);
				if (packet != null) {
				
				}
			}
		} catch (IOException e) {
			if (this.server.debug()) {
				e.printStackTrace();
			}
			server.info("User " + this.user.getUserName() + "(" + this.socket.getInetAddress().getHostAddress() + ") disconnected...");
			return;
		}
					
	}
	
}
