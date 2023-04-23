package cn.edu.sustech.cs209.chatclient.model;

import cn.edu.sustech.cs209.chatclient.net.ChatServer;
import cn.edu.sustech.cs209.chatclient.net.UserThread;
import com.alibaba.fastjson.JSON;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {
	
	private final ChatServer server;
	
	private Map<String, UserThread> onlineUsers;
	private Map<String, User> users;
	
	public User createUser(String username, String password) {
		if (users.containsKey(username.toLowerCase())) {
			return new User(null, null, null);
		}
		User user = new User(username, password, new UserPI(username));
		users.put(username.toLowerCase(), user);
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
	
	public User loginUser(String username, String password, UserThread thread) {
		User user = users.get(username.toLowerCase());
		if (user != null && user.getPassword().equals(password)) {
			onlineUsers.put(username.toLowerCase(), thread);
			return user;
		}
		return null;
	}
	
	public Collection<UserPI> getUserPIs() {
		return users.values().stream().map(User::getUserPI).toList();
	}
	
	public User getUser(String username) {
		return users.get(username.toLowerCase());
	}
	
	public Collection<String> getOnlineUserPIs() {
		return onlineUsers.keySet().stream().map(e -> getUser(e).getUserName()).toList();
	}
	
	public UserThread getOnlineUserThread(String username) {
		return onlineUsers.get(username.toLowerCase());
	}
	
	public void saveUser(User user) {
		File file = new File("users" + File.separator + user.getUserName().toLowerCase() + ".json");
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
			out.write(JSON.toJSONString(user).getBytes(StandardCharsets.UTF_8));
			out.flush();
			out.close();
		} catch (Exception e) {
			if (this.server.debug()) {
				e.printStackTrace();
			}
		}
	}
	
	public Collection<UserThread> getOnlineUserThreads() {
		return onlineUsers.values();
	}
	
	synchronized public boolean logoutUser(String username) {
		UserThread thread = onlineUsers.get(username.toLowerCase());
		if (thread != null) {
			if (!thread.isClosed()) {
				try {
					thread.closeSocket();
				} catch (Exception e) {
					if (this.server.debug()) {
						e.printStackTrace();
					}
				}
			}
			onlineUsers.remove(username.toLowerCase());
			return true;
		}
		return false;
	}
	
	public void init() {
		File dir = new File("users");
		if (!dir.exists()) {
			if (!dir.mkdir()) {
				server.error("Failed to create user profile directory.");
				return;
			}
		}
		
		for (File file : dir.listFiles(e -> e.getName().toLowerCase().endsWith(".json"))) {
			try {
				BufferedInputStream bufferedInputStream = new BufferedInputStream(
					new FileInputStream(file));
				User user = JSON.parseObject(
					new String(bufferedInputStream.readAllBytes(), StandardCharsets.UTF_8),
					User.class);
				users.put(user.getUserName().toLowerCase(), user);
				server.info("Loaded user profile: " + user.getUserName());
			} catch (Exception e) {
				server.warning("Failed to load user profile: " + file.getName());
			}
			
		}
	}
	
	
	public UserManager(ChatServer server) {
		this.server = server;
		
		this.users = new ConcurrentHashMap<>();
		this.onlineUsers = new ConcurrentHashMap<>();
		
		
	}
	
}
