package cn.edu.sustech.cs209.chatclient.model;

import java.net.Socket;

public class User {
	
	
	private String username;
	private String password;
	private UserPI userPI;
	
	public User(String username, String password, UserPI userPI) {
		this.username = username;
		this.password = password;
		this.userPI = userPI;
	}
	
	public UserPI getUserPI() {
		return this.userPI;
	}
	
	public String getUserName() {
		return this.username;
	}
	
	public String getPassword() {
		return this.password;
	}
	
}
