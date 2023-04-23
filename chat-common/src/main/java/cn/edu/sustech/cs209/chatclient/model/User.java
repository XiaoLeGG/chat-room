package cn.edu.sustech.cs209.chatclient.model;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class User {
	
	
	private String username;
	private String password;
	private UserPI userPI;
	private List<Integer> chatRooms;
	
	public User(String username, String password, UserPI userPI) {
		this.username = username;
		this.password = password;
		this.userPI = userPI;
		this.chatRooms = new ArrayList<>();
	}
	
	public void addChatRoom(int roomID) {
		this.chatRooms.add(roomID);
	}
	
	public List<Integer> getChatRooms() {
		return this.chatRooms;
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
