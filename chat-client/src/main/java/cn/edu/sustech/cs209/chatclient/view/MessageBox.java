package cn.edu.sustech.cs209.chatclient.view;

import cn.edu.sustech.cs209.chatclient.model.ChatInformation;
import cn.edu.sustech.cs209.chatclient.model.ChatInformation.ChatInformationType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class MessageBox extends HBox {
	
	private ChatInformation content;
	private boolean self;
	
	public MessageBox(ChatInformation content, boolean self) {
		super();
		this.content = content;
		this.self = self;
		this.init();
		
	}
	
	private void init() {
		this.getStylesheets().add(getClass().getResource("/css/message_box.css").toExternalForm());
		this.getStyleClass().add("message-box");
		VBox vbox = new VBox();
		vbox.setSpacing(10);
		Label senderName = new Label(this.content.getSender().getName());
		senderName.getStyleClass().add("name-label");
		vbox.getChildren().add(senderName);
		if (this.content.getType() == ChatInformationType.MESSAGE) {
			String message = this.content.getContent().getString("message");
			TextFlow field = new TextFlow(new Text(message));
			if (this.self) {
				field.getStyleClass().add("message-field-right");
			} else {
				field.getStyleClass().add("message-field-left");
			}
			field.setMaxWidth(200);
			field.setPadding(new Insets(10, 10, 10, 10));
			
			//JFXDepthManager.setDepth(field, 1);
			vbox.getChildren().add(field);
			
		}
		if (this.self) {
			vbox.setAlignment(Pos.CENTER_RIGHT);
			this.setAlignment(Pos.CENTER_RIGHT);
		} else {
			vbox.setAlignment(Pos.CENTER_LEFT);
			this.setAlignment(Pos.CENTER_LEFT);
		}
		this.setPadding(new Insets(10, 15, 10, 20));
		this.getChildren().add(vbox);
	}
	
}
