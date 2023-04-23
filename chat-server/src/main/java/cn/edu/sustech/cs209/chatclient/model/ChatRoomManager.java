package cn.edu.sustech.cs209.chatclient.model;

import cn.edu.sustech.cs209.chatclient.model.ChatRoom.RoomType;
import cn.edu.sustech.cs209.chatclient.net.ChatServer;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatRoomManager {

	private ChatServer server;
	private Map<Integer, ChatRoom> rooms;
	private Map<Integer, ChatRoomHistory> histories;
	
	public ChatRoomManager(ChatServer server) {
		this.server = server;
	}
	
	public ChatRoom getChatRoom(int id) {
		return rooms.get(id);
	}
	
	public ChatRoomHistory getChatRoomHistory(int id) {
		return histories.get(id);
	}
	
	public void saveChatRoom(int id) {
		ChatRoom room = rooms.get(id);
		ChatRoomHistory history = histories.get(id);
		File file = new File("chatrooms" + File.separator + id + ".json");
		JSONObject object = new JSONObject();
		object.put("room", JSON.toJSON(room));
		object.put("history", JSON.toJSON(history));
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			byte[] bytes = object.toJSONString().getBytes(StandardCharsets.UTF_8);
			FileOutputStream output = new FileOutputStream(file);
			output.write(bytes);
			output.flush();
			output.close();
		} catch (Exception e) {
			if (this.server.debug()) {
				e.printStackTrace();
			}
		}
	}
	
	public Collection<ChatRoom> getChatRooms() {
		return rooms.values();
	}
	
	synchronized public ChatRoom createChatRoom(String title, RoomType type, String ...users) {
		if (users.length < 2) {
			return null;
		}
		
		if (type == RoomType.PRIVATE) {
			if (users.length > 2) {
				return null;
			}
			for (ChatRoom room : rooms.values()) {
				if (room.getType() == RoomType.PRIVATE) {
					if ((users[0].equals(room.getUsers()[0]) && users[1].equals(room.getUsers()[1])) || (users[0].equals(room.getUsers()[1]) && users[1].equals(room.getUsers()[0]))) {
						return null;
					}
				}
			}
		}
		int id = rooms.size() + 1;
		ChatRoom room = new ChatRoom(title, id, type, users);
		ChatRoomHistory history = new ChatRoomHistory(new ArrayList<>());
		File file = new File("chatrooms" + File.separator + id + ".json");
		JSONObject object = new JSONObject();
		object.put("room", JSON.toJSON(room));
		object.put("history", JSON.toJSON(history));
		try {
			file.createNewFile();
			byte[] bytes = object.toJSONString().getBytes(StandardCharsets.UTF_8);
			FileOutputStream output = new FileOutputStream(file);
			output.write(bytes);
			output.flush();
			output.close();
		} catch (Exception e) {
			if (this.server.debug()) {
				e.printStackTrace();
			}
			return null;
		}
		rooms.put(id, room);
		histories.put(id, history);
		return room;
	}
	
	public void init() {
		rooms = new ConcurrentHashMap<>();
		histories = new ConcurrentHashMap<>();
		File dir = new File("chatrooms");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		for (File file : dir.listFiles(e -> e.getName().toLowerCase().endsWith(".json"))) {
			try {
				BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
				byte bytes[] = input.readAllBytes();
				JSONObject content = JSON.parseObject(new String(bytes, StandardCharsets.UTF_8));
				ChatRoom room = (ChatRoom) JSON.parseObject(content.get("room").toString(), ChatRoom.class);
				ChatRoomHistory fullHistory = (ChatRoomHistory) JSON.parseObject(content.get("history").toString(), ChatRoomHistory.class);
				rooms.put(room.getRoomID(), room);
				histories.put(room.getRoomID(), fullHistory);
				this.server.info("Loaded chatroom " + room.getRoomID() + " from file " + file.getName());
			} catch (Exception e) {
				if (this.server.debug()) {
					e.printStackTrace();
				}
				this.server.warning("Failed to load chatroom file " + file.getName());
			}
		}
	}

}
