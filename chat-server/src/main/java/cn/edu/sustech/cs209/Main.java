package cn.edu.sustech.cs209;

import cn.edu.sustech.cs209.net.ChatServer;
import cn.edu.sustech.cs209.packet.NetConfig;

public class Main {
	
	public static void main(String[] args) {
		ChatServer server = new ChatServer();
		try {
			server.initServer(NetConfig.DEFAULT_SERVER_HOST, NetConfig.SERVER_PORT);
			server.startServer();
		} catch (Exception e) {
			if (server.debug()) {
				e.printStackTrace();
			}
		}
	}
		
}
