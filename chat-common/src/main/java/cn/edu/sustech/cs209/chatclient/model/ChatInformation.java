package cn.edu.sustech.cs209.chatclient.model;

import com.alibaba.fastjson.JSONObject;

public class ChatInformation {
	
	private ChatInformationType type;
	private UserPI sender;
	private JSONObject content;
	private long timestamp;
	
	
	public ChatInformationType getType() {
		return type;
	}
	
	public UserPI getSender() {
		return sender;
	}
	
	public JSONObject getContent() {
		return content;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public ChatInformation(ChatInformationType type, UserPI sender, JSONObject content, long timestamp) {
		this.type = type;
		this.sender = sender;
		this.content = content;
		this.timestamp = timestamp;
	}
	
	public enum ChatInformationType {
		MESSAGE, FILE;
	}
	
}
