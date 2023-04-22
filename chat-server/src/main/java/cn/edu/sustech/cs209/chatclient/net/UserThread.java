package cn.edu.sustech.cs209.chatclient.net;

import cn.edu.sustech.cs209.chatclient.model.ChatInformation;
import cn.edu.sustech.cs209.chatclient.model.ChatRoom;
import cn.edu.sustech.cs209.chatclient.model.ChatRoom.RoomType;
import cn.edu.sustech.cs209.chatclient.model.ChatRoomHistory;
import cn.edu.sustech.cs209.chatclient.model.User;
import cn.edu.sustech.cs209.chatclient.model.UserManager;
import cn.edu.sustech.cs209.chatclient.packet.PacketIO;
import cn.edu.sustech.cs209.chatclient.packet.Packet;
import cn.edu.sustech.cs209.chatclient.packet.PacketType;
import cn.edu.sustech.cs209.chatclient.packet.PacketWrapper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class UserThread extends Thread {
	
	private Socket socket;
	private ChatServer server;
	private User user;
	private BufferedReader reader;
	private BufferedWriter writer;
	
	public UserThread(Socket socket, ChatServer server) {
		this.socket = socket;
		this.server = server;
	}
	
	public boolean isClosed() {
		return this.socket == null || this.socket.isClosed();
	}
	
	public void closeSocket() throws IOException {
		if (this.socket == null) {
			return;
		}
		if (!this.socket.isClosed()) {
			this.socket.close();
		}
	}
	
	public User getUser() {
		return this.user;
	}
	
	@Override
	public void run() {
		try {
			this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			this.writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
			Packet packet = PacketIO.receivePacket(this.reader);
			if (packet == null) {
				this.socket.close();
				return;
			}
			if (packet.getType() == PacketType.LOGIN) {
				this.server.info("Receive login packet from " + socket.getInetAddress());
				JSONObject content = packet.getContent();
				if (!content.containsKey("username") || !content.containsKey("password")) {
					this.socket.close();
					return;
				}
				String username = content.getString("username");
				String password = content.getString("password");
				this.user = this.server.getUserManager().loginUser(username, password, this);
				if (user == null) {
					PacketIO.sendPacket(this.writer, new Packet(PacketType.LOGIN, "server", 0, 0, new JSONObject()));
					this.socket.close();
					return;
				} else {
					PacketIO.sendPacket(this.writer, new Packet(PacketType.LOGIN, "server", 0, 1, (JSONObject) JSON.toJSON(user)));
					PacketIO.sendPacket(this.writer, PacketUtils.createServerUsersListPacket(this.server));
					this.server.broadcastPacket(PacketUtils.createOnlineUsersListPacket(this.server));
					this.server.info("User " + user.getUserName() + " logged in");
					this.startReceive();
				}
			}
			if (packet.getType() == PacketType.REGISTER) {
				this.server.info("Receive register packet from " + socket.getInetAddress());
				JSONObject content = packet.getContent();
				if (!content.containsKey("username") || !content.containsKey("password")) {
					return;
				}
				String username = content.getString("username");
				String password = content.getString("password");
				User user = this.server.getUserManager().createUser(username, password);
				if (user == null) {
					PacketIO.sendPacket(this.writer,
						new Packet(PacketType.REGISTER, "server", 0, 0, new JSONObject()));
					this.socket.close();
					return;
				}
				if (user.getUserName() == null) {
					PacketIO.sendPacket(this.writer,
						new Packet(PacketType.REGISTER, "server", 0, 1, JSON.parseObject("{\"msg\": \"用户名已存在\"}")));
					this.socket.close();
					return;
				}
				PacketIO.sendPacket(this.writer,
					new Packet(PacketType.REGISTER, "server", 0, 2, (JSONObject) JSON.toJSON(user)));
				this.socket.close();
				this.server.broadcastPacket(PacketUtils.createServerUsersListPacket(this.server));
				this.server.info("User " + user.getUserName() + " registered");
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
			while (true) {
				Packet packet = PacketIO.receivePacket(this.reader);
				if (packet != null) {
					if (packet.getType() == PacketType.MESSAGE) {
						this.server.info("Receive message packet from " + this.user.getUserName());
						ChatInformation ci = (ChatInformation) JSON.parseObject(packet.getContent().getJSONObject("ci").toJSONString(), ChatInformation.class);
						ChatRoom chatRoom = this.server.getChatRoomManager().getChatRoom(packet.getContent().getInteger("room"));
						if (chatRoom == null) {
							continue;
						}
						Packet send = new Packet(PacketType.MESSAGE, "server", 0, 0, packet.getContent());
						for (String user : chatRoom.getUsers()) {
							UserThread thread = this.server.getUserManager().getOnlineUserThread(user);
							if (thread == null) {
								continue;
							}
							thread.sendPacket(send);
						}
					}
					if (packet.getType() == PacketType.CHAT_ROOM) {
						this.server.info("Receive chat room packet from " + this.user.getUserName());
						if (packet.getSubCode() == 0) {
							JSONArray content = packet.getContent().getJSONArray("users");
							String[] users = new String[content.size()];
							users = (String[]) content.toArray(users);
							String title = packet.getContent().getString("title");
							RoomType type = RoomType.valueOf(packet.getContent().getString("type"));
							ChatRoom chatRoom = this.server.getChatRoomManager().createChatRoom(title, type, users);
							if (chatRoom == null) {
								JSONObject object = new JSONObject();
								object.put("msg", "创建失败");
								Packet send = new Packet(PacketType.CHAT_ROOM, "server", 0, 0, new JSONObject());
								this.sendPacket(send);
								continue;
							}
							JSONObject object = new JSONObject();
							object.put("room", JSON.toJSON(chatRoom));
							object.put("history", JSON.toJSON(new ChatRoomHistory(new ArrayList<>())));
							Packet send = new Packet(PacketType.CHAT_ROOM, "server", 0, 1, object);
							for (String user : chatRoom.getUsers()) {
								UserThread u = this.server.getUserManager().getOnlineUserThread(user);
								if (u != null) {
									u.sendPacket(send);
								}
							}
						}
					}
				}
			}
		} catch (IOException e) {
			if (this.server.debug()) {
				e.printStackTrace();
			}
			server.info("User " + this.user.getUserName() + "(" + this.socket.getInetAddress().getHostAddress() + ") disconnected...");
			this.server.getUserManager().logoutUser(this.user.getUserName());
			this.server.broadcastPacket(PacketUtils.createOnlineUsersListPacket(this.server));
			return;
		}
	}
	
	public boolean sendPacket(Packet packet) throws IOException {
		if (this.socket == null || this.socket.isClosed()) {
			return false;
		}
		PacketIO.sendPacket(this.writer, packet);
		return true;
	}
	
}
