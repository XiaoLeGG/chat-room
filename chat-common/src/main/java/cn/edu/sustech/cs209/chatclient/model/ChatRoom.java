package cn.edu.sustech.cs209.chatclient.model;

public class ChatRoom {
	
	private String name;
	private int roomID;
	private RoomType type;
	private String[] users;
	
	public String getName() {
		return name;
	}
	
	public int getRoomID() {
		return roomID;
	}
	
	public RoomType getType() {
		return type;
	}
	
	public String[] getUsers() {
		return users;
	}
	
	public ChatRoom(String name, int roomID, RoomType type, String ...users) {
		this.name = name;
		this.roomID = roomID;
		this.type = type;
		this.users = users;
	}
	
	public enum RoomType {
		PRIVATE, GROUP, EMPTY;
	}
	
}
