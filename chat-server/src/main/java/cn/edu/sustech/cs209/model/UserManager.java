package cn.edu.sustech.cs209.model;

import cn.edu.sustech.cs209.net.ChatServer;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class UserManager {
	private final ChatServer server;
	
	private Map<String, Socket> onlineUsers;
	private Map<String, User> profiles;

	public User createUser(String username, String password) {
		if (profiles.containsKey(username.toLowerCase())) {
			return new User(null, null);
		}
		User user = new User(username, password);
		profiles.put(username.toLowerCase(), user);
		File file = new File("users" + File.separator + username.toLowerCase() + ".json");
		try {
			if (!file.createNewFile()) {
				this.server.error("Failed to create user(" + username + ") profile file.");
				return null;
			}
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
			out.write(JSON.toJSONString(user).getBytes(StandardCharsets.UTF_8));
			out.flush();
			out.close();
		} catch (Exception e) {
			if (this.server.debug()) {
				e.printStackTrace();
			}
			return null;
		}
		return user;
	}
	
	public User loginUser(String username, String password, Socket socket) {
		User user = profiles.get(username.toLowerCase());
		if (user != null && user.getPassword().equals(password)) {
			onlineUsers.put(username.toLowerCase(), socket);
			return user;
		}
		return null;
	}
	
	public UserManager(ChatServer server) {
		this.server = server;
		
		this.profiles = new HashMap<>();
		this.onlineUsers = new HashMap<>();
		
		File dir = new File("users");
		if (!dir.exists()) {
			if (!dir.mkdir()) {
				server.error("Failed to create user profile directory.");
				return;
			}
		}
		
		for (File file : dir.listFiles(e -> e.getName().toLowerCase().endsWith(".json"))) {
			try {
				BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
				User user = JSON.parseObject(new String(bufferedInputStream.readAllBytes(), StandardCharsets.UTF_8), User.class);
				profiles.put(user.getUserName().toLowerCase(), user);
				server.info("Loaded user profile: " + user.getUserName());
			} catch (Exception e) {
				server.warning("Failed to load user profile: " + file.getName());
			}
			
		}
		
	}

}
