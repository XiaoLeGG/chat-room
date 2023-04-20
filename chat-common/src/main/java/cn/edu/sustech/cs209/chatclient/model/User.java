package cn.edu.sustech.cs209.chatclient.model;

import java.net.Socket;

public class User {
	
	
	private String username;
	private String password;
	
	public User(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public String getUserName() {
		return this.username;
	}
	
	public String getPassword() {
		return this.password;
	}
	
}
