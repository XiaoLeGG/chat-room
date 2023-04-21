package cn.edu.sustech.cs209.chatclient.view;

import cn.edu.sustech.cs209.chatclient.controller.ChatController;

import cn.edu.sustech.cs209.chatclient.model.ChatInformation;
import cn.edu.sustech.cs209.chatclient.model.ChatInformation.ChatInformationType;
import cn.edu.sustech.cs209.chatclient.model.ChatRoom;
import cn.edu.sustech.cs209.chatclient.model.ChatRoom.RoomType;
import cn.edu.sustech.cs209.chatclient.model.ChatRoomHistory;
import cn.edu.sustech.cs209.chatclient.model.UserPI;
import com.alibaba.fastjson.JSONObject;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.effects.JFXDepthManager;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

public class ChatPane {
	
	private ChatController chatController;
	private Stage stage;
	private Scene scene;
	private StackPane pane;
	private ChatSplitPane emptyChatBlock;
	private HBox mainBox;
	private HashMap<Integer, ChatSplitPane> chatBlockMap;
	
	public ChatPane(ChatController chatController) {
		this.chatController = chatController;
		this.chatBlockMap = new HashMap<>();
	}
	
	public void show() {
		this.stage.show();
	}
	
	public void hide() {
		this.stage.hide();
	}
	
	public void switchChatContentPane(int roomID) {
		if (this.chatBlockMap.get(roomID) == null) {
			return;
		}
		this.mainBox.getChildren().remove(1);
		this.mainBox.getChildren().add(this.chatBlockMap.get(roomID));
	}
	
	public void appendChatRoom(ChatRoom room, ChatRoomHistory history) {
		ChatSplitPane chatBlock = this.createChatContentPane(room, history);
		this.chatBlockMap.put(room.getRoomID(), chatBlock);
		// Add list view
	}
	
	public ChatSplitPane createChatContentPane(ChatRoom room, ChatRoomHistory history) {
		return new ChatSplitPane(room, history, room.getType() == RoomType.EMPTY ? null : this.chatController.getUser().getUserPI());
	}
	
	public void init() {
		this.stage = new Stage();
		this.pane = new StackPane();
		this.scene = new Scene(pane);
		this.stage.setScene(scene);
		this.pane.getStylesheets().add(getClass().getResource("/css/chat_pane.css").toExternalForm());
		this.pane.getStyleClass().add("main-background");
		
		this.mainBox = new HBox();
		VBox left = new VBox();
		this.mainBox.setSpacing(50);
		left.setSpacing(40);
		
		StackPane profileBlock = new StackPane();
		profileBlock.getStyleClass().add("profile-block");
		JFXDepthManager.setDepth(profileBlock, 3);
		
		StackPane chatListBlock = new StackPane();
		chatListBlock.getStyleClass().add("chat-list-block");
		JFXDepthManager.setDepth(chatListBlock, 3);
		
		left.getChildren().addAll(profileBlock, chatListBlock);
		
		this.emptyChatBlock = createChatContentPane(new ChatRoom("", 0, RoomType.EMPTY), new ChatRoomHistory(new ArrayList<>()));
		
		this.mainBox.getChildren().addAll(left, emptyChatBlock);
		
		left.setAlignment(Pos.CENTER);
		this.mainBox.setAlignment(Pos.CENTER);
		StackPane.setAlignment(this.mainBox, Pos.CENTER);
		this.pane.getChildren().add(this.mainBox);
		
	}
	
	
	
}
