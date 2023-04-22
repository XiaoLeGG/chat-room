package cn.edu.sustech.cs209.chatclient.model;

import com.alibaba.fastjson.JSONObject;

public class ChatInformation {
	
	private ChatInformationType type;
	private String sender;
	private JSONObject content;
	private long timestamp;
	
	
	public ChatInformationType getType() {
		return type;
	}
	
	public String getSender() {
		return sender;
	}
	
	public JSONObject getContent() {
		return content;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public ChatInformation(ChatInformationType type, String sender, JSONObject content, long timestamp) {
		this.type = type;
		this.sender = sender;
		this.content = content;
		this.timestamp = timestamp;
	}
	
	public enum ChatInformationType {
		MESSAGE, FILE;
	}
	
}
