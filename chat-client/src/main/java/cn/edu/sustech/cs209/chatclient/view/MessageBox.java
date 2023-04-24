package cn.edu.sustech.cs209.chatclient.view;

import cn.edu.sustech.cs209.chatclient.MainApplication;
import cn.edu.sustech.cs209.chatclient.model.ChatInformation;
import cn.edu.sustech.cs209.chatclient.model.ChatInformation.ChatInformationType;
import cn.edu.sustech.cs209.chatclient.packet.NetConfig;
import cn.edu.sustech.cs209.chatclient.packet.Packet;
import cn.edu.sustech.cs209.chatclient.packet.PacketIO;
import cn.edu.sustech.cs209.chatclient.packet.PacketType;
import com.alibaba.fastjson.JSONObject;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;

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
		Label senderName = new Label(this.content.getSender());
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
		if (this.content.getType() == ChatInformationType.FILE) {
			String name = this.content.getContent().getString("file");
			String remote = this.content.getContent().getString("remote-file");
			long length = this.content.getContent().getLong("length");
			
			VBox fileBox = new VBox();
			fileBox.setPrefWidth(200);
			fileBox.setPadding(new Insets(10, 10, 10, 10));
			fileBox.setAlignment(Pos.CENTER);
			Label fileName = new Label(name);
			Label fileLength = new Label();
			if (length > 1024 * 1024) {
				fileLength.setText(name + " (" + String.format("%.2f", (double) length / 1024 / 1024) + "mb)");
			} else if (length > 1024) {
				fileLength.setText(name + " (" + String.format("%.2f", length / 1024) + "kb)");
			} else {
				fileLength.setText(name + " (" + length + "b)");
			}
			fileBox.getChildren().addAll(fileName, fileLength);
			fileBox.getStyleClass().add("file-field");
			fileBox.setOnMouseEntered(event -> {
				fileBox.setCursor(Cursor.HAND);
			});
			fileBox.setOnMouseExited(event -> {
				fileBox.setCursor(Cursor.DEFAULT);
			});
			fileBox.setOnMouseClicked(e -> {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("存储文件");
				fileChooser.setInitialFileName(name);
				File file = fileChooser.showSaveDialog(this.getScene().getWindow());
				if (file != null) {
					Thread thread = new Thread() {
						@Override
						public void run() {
							Socket socket = new Socket();
							try {
								if (!file.exists()) {
									file.createNewFile();
								}
								socket.connect(new InetSocketAddress(NetConfig.DEFAULT_SERVER_HOST,
									NetConfig.SERVER_DOWNLOAD_PORT), 10000);
								JSONObject object = new JSONObject();
								object.put("name", remote);
								Packet packet = new Packet(PacketType.FILE, "download", 2, 1,
									object);
								PacketIO.sendPacket(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), packet);
								DataInputStream input = new DataInputStream(socket.getInputStream());
								DataOutputStream output = new DataOutputStream(new FileOutputStream(
									file));
								byte[] bytes = new byte[1024];
								while (input.read(bytes) != -1) {
									output.write(bytes);
								}
								output.close();
								socket.close();
							} catch (Exception ex) {
								if (MainApplication.debug()) {
									ex.printStackTrace();
								}
							}
						}
					};
					thread.setDaemon(true);
					thread.start();
				}
			});
			
			//JFXDepthManager.setDepth(field, 1);
			vbox.getChildren().add(fileBox);
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
