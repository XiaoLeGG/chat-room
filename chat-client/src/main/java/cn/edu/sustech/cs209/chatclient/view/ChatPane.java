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
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.effects.JFXDepthManager;
import com.jfoenix.validation.RequiredFieldValidator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

public class ChatPane {
	
	private ChatController chatController;
	private Stage stage;
	private Scene scene;
	private StackPane pane;
	private ChatSplitPane emptyChatBlock;
	private HBox mainBox;
	private HashMap<Integer, ChatSplitPane> chatBlockMap;
	private ListView<ChatRoom> chatRoomListView;
	
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
		return new ChatSplitPane(room, history,
			room.getType() == RoomType.EMPTY ? null : this.chatController.getUser().getUserPI());
	}
	
	public void init() {
		this.stage = new Stage();
		
		this.pane = new StackPane();
		this.scene = new Scene(pane);
		this.stage.setScene(scene);
		this.pane.getStylesheets()
			.add(getClass().getResource("/css/chat_pane.css").toExternalForm());
		this.pane.getStyleClass().add("main-background");
		
		this.mainBox = new HBox();
		VBox left = new VBox();
		this.mainBox.setSpacing(50);
		left.setSpacing(40);
		
		StackPane profileBlock = new StackPane();
		profileBlock.getStyleClass().add("profile-block");
		JFXDepthManager.setDepth(profileBlock, 3);
		Label userNameLabel = new Label(this.chatController.getUser().getUserName());
		userNameLabel.getStyleClass().add("user-name-label");
		profileBlock.getChildren().add(userNameLabel);
		profileBlock.setAlignment(Pos.CENTER_LEFT);
		profileBlock.setPadding(new Insets(0, 0, 0, 25));
		
		SplitPane chatListBlock = new SplitPane();
		chatListBlock.setOrientation(Orientation.VERTICAL);
		chatListBlock.setDividerPositions(0.1);
		chatListBlock.getStyleClass().add("chat-list-block");
		JFXDepthManager.setDepth(chatListBlock, 3);
		HBox buttonBox = new HBox();
		JFXButton newChatButton = new JFXButton("新建会话");
		newChatButton.getStyleClass().add("new-chat-button");
		newChatButton.setOnAction(e -> {
			Stage subStage = new Stage();
			subStage.initModality(Modality.APPLICATION_MODAL);
			subStage.initOwner(this.stage);
			StackPane subPane = new StackPane();
			Scene subScene = new Scene(subPane, 400, 600);
			subStage.setScene(subScene);
			subPane.getStylesheets()
				.add(getClass().getResource("/css/chat_pane.css").toExternalForm());
			subPane.setAlignment(Pos.TOP_CENTER);
			subPane.setPadding(new Insets(10, 10, 10, 10));
			VBox vbox = new VBox();
			subPane.getChildren().add(vbox);
			vbox.setAlignment(Pos.TOP_LEFT);
			Label title = new Label("新建会话");
			title.getStyleClass().add("new-chat-title");
			
			HBox hbox = new HBox();
			hbox.setAlignment(Pos.CENTER_LEFT);
			Label tag = new Label("会话名称");
			JFXTextField name = new JFXTextField();
			name.getValidators().add(new RequiredFieldValidator("会话名称不能为空"));
			hbox.getChildren().addAll(tag, name);
			hbox.setSpacing(5);
			
			List<String> users = new ArrayList<>();
			
			ScrollPane scrollPane = new ScrollPane();
			ListView<UserPI> listView = new ListView<>();
			listView.setStyle("-fx-background-color: transparent");
			listView.setCellFactory(new Callback<ListView<UserPI>, ListCell<UserPI>>() {
				@Override
				public ListCell<UserPI> call(ListView<UserPI> param) {
					return new ListCell<UserPI>() {
						@Override
						protected void updateItem(UserPI item, boolean empty) {
							super.updateItem(item, empty);
							if (empty || item == null) {
								setGraphic(null);
								return;
							}
							HBox hbox = new HBox();
							hbox.setSpacing(10);
							hbox.setAlignment(Pos.CENTER_LEFT);
							TextFlow flow = new TextFlow();
							Text text = new Text(item.getName());
							Text status = new Text(" ◉ ");
							if (chatController.isOnline(item.getName())) {
								status.setStyle("-fx-text-fill: #06C179");
							} else {
								status.setStyle("-fx-text-fill: #C1C1C1");
							}
							flow.getChildren().addAll(text, status);
							JFXCheckBox checkBox = new JFXCheckBox();
							checkBox.setOnAction(e -> {
								if (checkBox.isSelected()) {
									users.add(item.getName());
								} else {
									users.remove(item.getName());
								}
							});
							hbox.getChildren().addAll(flow, checkBox);
							setGraphic(hbox);
							
						}
					};
				}
			});
			listView.getItems().add(0, chatController.getUser().getUserPI());
			listView.refresh();
			scrollPane.setContent(listView);
			
			vbox.setSpacing(20);
			vbox.getChildren().addAll(title, hbox, scrollPane);
			
			subStage.show();
		});
		
		this.chatRoomListView = new ListView<>();
		this.chatRoomListView.getStyleClass().add("chat-room-list-view");
		this.chatRoomListView.getStylesheets()
			.add(getClass().getResource("/css/scroll_bar.css").toExternalForm());
		
		buttonBox.getChildren().add(newChatButton);
		buttonBox.setPadding(new Insets(10, 10, 10, 10));
		buttonBox.maxHeightProperty().bind(chatListBlock.widthProperty().multiply(0.1));
		chatListBlock.getItems().addAll(buttonBox, this.chatRoomListView);
		
		left.getChildren().addAll(profileBlock, chatListBlock);
		
		this.emptyChatBlock = createChatContentPane(new ChatRoom("", 0, RoomType.EMPTY),
			new ChatRoomHistory(new ArrayList<>()));
		
		this.mainBox.getChildren().addAll(left, emptyChatBlock);
		
		left.setAlignment(Pos.CENTER);
		this.mainBox.setAlignment(Pos.CENTER);
		StackPane.setAlignment(this.mainBox, Pos.CENTER);
		this.pane.getChildren().add(this.mainBox);
		this.stage.focusedProperty().addListener(e -> {
			if (this.stage.isFocused()) {
				newChatButton.setDisable(true);
				Platform.runLater(() -> newChatButton.setDisable(false));
			}
		});
		
	}
	
	
}
