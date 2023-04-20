package cn.edu.sustech.cs209.chatclient.view;

import cn.edu.sustech.cs209.chatclient.controller.ChatController;

import com.jfoenix.effects.JFXDepthManager;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
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
	
	public ChatPane(ChatController chatController) {
		this.chatController = chatController;
	}
	
	public void show() {
		this.stage.show();
	}
	
	public void hide() {
		this.stage.hide();
	}
	
	public void init() {
		this.stage = new Stage();
		this.pane = new StackPane();
		this.scene = new Scene(pane);
		this.stage.setScene(scene);
		this.pane.getStylesheets().add(getClass().getResource("/css/chat_pane.css").toExternalForm());
		this.pane.getStyleClass().add("main-background");
		
		HBox hbox = new HBox();
		VBox left = new VBox();
		hbox.setSpacing(50);
		left.setSpacing(40);
		
		StackPane profileBlock = new StackPane();
		profileBlock.getStyleClass().add("profile-block");
		JFXDepthManager.setDepth(profileBlock, 3);
		
		StackPane chatListBlock = new StackPane();
		chatListBlock.getStyleClass().add("chat-list-block");
		JFXDepthManager.setDepth(chatListBlock, 3);
		
		left.getChildren().addAll(profileBlock, chatListBlock);
		
		SplitPane chatBlock = new SplitPane();
		chatBlock.setOrientation(Orientation.VERTICAL);
		chatBlock.setDividerPositions(0.1, 0.75);
		chatBlock.getStyleClass().add("chat-block");
		JFXDepthManager.setDepth(chatBlock, 3);
		
		HBox chatHeader = new HBox();
		ScrollPane chatContent = new ScrollPane();
		chatContent.getStylesheets().add(getClass().getResource("/css/scroll_bar.css").toExternalForm());
		
		VBox chatComponentBox = new VBox();
		chatComponentBox.setSpacing(10);
		for (int i = 0; i < 10; ++i) {
			HBox chatComponent = new HBox();
			//chatComponent.getStyleClass().add("chat-component");
			TextField field = new TextField();
			field.setPrefSize(100, 50);
			field.setStyle("-fx-background-color: SKYBLUE;");
			chatComponent.getChildren().add(field);
			JFXDepthManager.setDepth(chatComponent, 3);
			chatComponentBox.getChildren().add(chatComponent);
		}
		chatContent.setContent(chatComponentBox);
		
		VBox chatTypeBox = new VBox();
		chatBlock.getItems().addAll(chatHeader, chatContent, chatTypeBox);
		
		hbox.getChildren().addAll(left, chatBlock);
		
		left.setAlignment(Pos.CENTER);
		hbox.setAlignment(Pos.CENTER);
		StackPane.setAlignment(hbox, Pos.CENTER);
		this.pane.getChildren().add(hbox);
		
	}
	
	
	
}
