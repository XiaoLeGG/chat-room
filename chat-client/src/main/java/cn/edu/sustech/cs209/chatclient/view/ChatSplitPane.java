package cn.edu.sustech.cs209.chatclient.view;

import cn.edu.sustech.cs209.chatclient.model.ChatInformation;
import cn.edu.sustech.cs209.chatclient.model.ChatRoom;
import cn.edu.sustech.cs209.chatclient.model.ChatRoom.RoomType;
import cn.edu.sustech.cs209.chatclient.model.ChatRoomHistory;
import cn.edu.sustech.cs209.chatclient.model.UserPI;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.effects.JFXDepthManager;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ChatSplitPane extends SplitPane {

	private ChatRoom room;
	private UserPI currentUser;
	private ChatRoomHistory history;
	
	private VBox chatComponentBox;
	
	public ChatSplitPane(ChatRoom room, ChatRoomHistory history, UserPI currentUser) {
		super();
		this.room = room;
		this.currentUser = currentUser;
		this.history = history;
		this.init();
	}
	
	public void init() {
		this.setOrientation(Orientation.VERTICAL);
		this.setDividerPositions(0.1, 0.75);
		this.getStyleClass().add("chat-block");
		JFXDepthManager.setDepth(this, 3);
		
		HBox chatHeader = new HBox();
		String title = this.room.getName();
		if (this.room.getType() == RoomType.PRIVATE) {
			title = this.room.getUsers()[0].equals(this.currentUser.getName()) ? this.room.getUsers()[1].getName() : this.room.getUsers()[0].getName();
		}
		Label titleLabel = new Label(title);
		titleLabel.getStyleClass().add("chat-room-title");
		chatHeader.getChildren().add(titleLabel);
		chatHeader.setPadding(new Insets(18, 10, 10, 20));
		
		ScrollPane chatContent = new ScrollPane();
		chatContent.getStylesheets().add(getClass().getResource("/css/scroll_bar.css").toExternalForm());
		
		this.chatComponentBox = new VBox();
		this.chatComponentBox.getStyleClass().add("chat-background");
		this.chatComponentBox.setSpacing(10);
		if (this.getRoom().getType() != RoomType.EMPTY) {
			this.history.stream().forEach((component) -> {
				MessageBox box = new MessageBox(component,
					component.getSender().getName().equals(this.currentUser.getName()));
				chatComponentBox.getChildren().add(box);
			});
		}
		
		chatContent.setContent(chatComponentBox);
		
		VBox chatTypeBox = new VBox();
		TextArea textArea = new TextArea();
		textArea.setWrapText(true);
		textArea.getStyleClass().add("type-area");
		textArea.setPadding(new Insets(20, 20, 10, 20));
		textArea.getStylesheets().add(getClass().getResource("/css/scroll_bar.css").toExternalForm());
		
		JFXButton send = new JFXButton("发送");
		send.getStyleClass().add("send-button");
		send.setAlignment(Pos.CENTER);
		send.setPadding(new Insets(5, 10, 5, 10));
		VBox.setMargin(send, new Insets(10, 20, 10, 540));
		
		chatTypeBox.getChildren().addAll(textArea, send);
		this.getItems().addAll(chatHeader, chatContent, chatTypeBox);
	}
	
	public void append(ChatInformation information) {
		if (this.getRoom().getType() == RoomType.EMPTY) {
			return;
		}
		MessageBox box = new MessageBox(information, information.getSender().getName().equals(this.currentUser.getName()));
		this.chatComponentBox.getChildren().add(box);
	}
	
	public ChatRoom getRoom() {
		return room;
	}

}