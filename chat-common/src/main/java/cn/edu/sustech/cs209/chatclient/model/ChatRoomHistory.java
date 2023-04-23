package cn.edu.sustech.cs209.chatclient.model;

import java.util.List;
import java.util.stream.Stream;

public class ChatRoomHistory {
	
	private List<ChatInformation> informationList;
	
	public ChatRoomHistory(List<ChatInformation> informationList) {
		this.informationList = informationList;
		this.informationList.sort((a, b) -> (int) (a.getTimestamp() - b.getTimestamp()));
	}
	
	public List<ChatInformation> getInformationList() {
		return this.informationList;
	}
	
	public void append(ChatInformation information) {
		this.informationList.add(information);
	}
	
	public Stream<ChatInformation> stream() {
		return this.informationList.stream();
	}
	
}
