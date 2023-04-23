package cn.edu.sustech.cs209.chatclient.net;

import cn.edu.sustech.cs209.chatclient.MainApplication;
import cn.edu.sustech.cs209.chatclient.packet.NetConfig;
import cn.edu.sustech.cs209.chatclient.packet.Packet;
import cn.edu.sustech.cs209.chatclient.packet.PacketIO;
import cn.edu.sustech.cs209.chatclient.packet.PacketType;
import com.alibaba.fastjson.JSONObject;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientConnector {

	private String userName;
	private String password;
	private Socket socket;
	private BufferedReader reader;
	private BufferedWriter writer;
	
	
	public ClientConnector(String username, String password) {
		this.userName = username;
		this.password = password;
	}
	
	public Packet login() {
		try {
			this.socket = new Socket();
			this.socket.setKeepAlive(true);
			this.socket.connect(new InetSocketAddress(NetConfig.DEFAULT_SERVER_HOST, NetConfig.SERVER_PORT), 1000 * 10);
			if (!socket.isConnected()) {
				return null;
			}
			this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			JSONObject content = new JSONObject();
			content.put("username", this.userName);
			content.put("password", this.password);
			Packet packet = new Packet(PacketType.LOGIN, this.userName, 1, 0, content);
			PacketIO.sendPacket(this.writer, packet);
			Packet receive = PacketIO.receivePacket(this.reader);
			return receive;
		} catch (Exception e) {
			if (MainApplication.debug()) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	public Packet accpetPacket() throws IOException {
		Packet packet = PacketIO.receivePacket(this.reader);
		return packet;
	}
	
	public void close() throws IOException {
		this.socket.close();
	}
	
	public Packet register() {
		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(NetConfig.DEFAULT_SERVER_HOST, NetConfig.SERVER_PORT), 1000 * 10);
			if (!socket.isConnected()) {
				return null;
			}
			this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			JSONObject content = new JSONObject();
			content.put("username", this.userName);
			content.put("password", this.password);
			Packet packet = new Packet(PacketType.REGISTER, this.userName, 1, 0, content);
			PacketIO.sendPacket(this.writer, packet);
			Packet receive = PacketIO.receivePacket(this.reader);
			return receive;
		} catch (Exception e) {
			if (MainApplication.debug()) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	public void sendPacket(Packet packet) throws IOException {
		PacketIO.sendPacket(this.writer, packet);
	}
}
