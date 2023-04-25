package cn.edu.sustech.cs209.chatclient.view;

import cn.edu.sustech.cs209.chatclient.MainApplication;
import cn.edu.sustech.cs209.chatclient.controller.ChatController;
import cn.edu.sustech.cs209.chatclient.model.ChatInformation;
import cn.edu.sustech.cs209.chatclient.model.ChatInformation.ChatInformationType;
import cn.edu.sustech.cs209.chatclient.model.ChatRoom;
import cn.edu.sustech.cs209.chatclient.model.ChatRoom.RoomType;
import cn.edu.sustech.cs209.chatclient.model.ChatRoomHistory;
import cn.edu.sustech.cs209.chatclient.model.UserPI;
import cn.edu.sustech.cs209.chatclient.packet.NetConfig;
import cn.edu.sustech.cs209.chatclient.packet.Packet;
import cn.edu.sustech.cs209.chatclient.packet.PacketIO;
import cn.edu.sustech.cs209.chatclient.packet.PacketType;
import com.alibaba.fastjson.JSONObject;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.effects.JFXDepthManager;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;

public class ChatSplitPane extends SplitPane {

  private ChatRoom room;
  private UserPI currentUser;
  private ChatRoomHistory history;

  private VBox chatComponentBox;
  private ChatController controller;
  private ScrollPane chatContent;

  public ChatSplitPane(
      ChatRoom room, ChatRoomHistory history, UserPI currentUser, ChatController controller) {
    super();
    this.room = room;
    this.currentUser = currentUser;
    this.history = history;
    this.controller = controller;
    this.init();
  }

  public void init() {
    this.setOrientation(Orientation.VERTICAL);
    this.setDividerPositions(0.1, 0.75);
    this.getStyleClass().add("chat-block");
    JFXDepthManager.setDepth(this, 3);

    String title = this.room.getName();
    if (this.room.getType() == RoomType.PRIVATE) {
      title =
          this.room.getUsers()[0].equals(this.currentUser.getName())
              ? this.room.getUsers()[1]
              : this.room.getUsers()[0];
    }
    Label titleLabel = new Label((this.room.getType() == RoomType.GROUP ? "群聊：" : "") + title);
    titleLabel.getStyleClass().add("chat-room-title");
    if (this.room.getType() == RoomType.GROUP) {
      StringBuilder builder = new StringBuilder();
      builder.append("群成员：\n");
      for (String user : this.room.getUsers()) {
        builder.append(user).append(", ");
      }
      builder.delete(builder.length() - 2, builder.length());
      Tooltip tip = new Tooltip(builder.toString());
      tip.setShowDelay(Duration.millis(10));
      titleLabel.setTooltip(tip);
    }
    HBox chatHeader = new HBox();
    chatHeader.getChildren().add(titleLabel);
    chatHeader.setPadding(new Insets(18, 10, 10, 20));

    this.chatContent = new ScrollPane();
    this.chatContent
        .getStylesheets()
        .add(getClass().getResource("/css/scroll_bar.css").toExternalForm());

    this.chatComponentBox = new VBox();
    this.chatComponentBox.getStyleClass().add("chat-background");
    this.chatComponentBox.setSpacing(10);
    if (this.getRoom().getType() != RoomType.EMPTY) {
      this.history.stream()
          .forEach(
              (component) -> {
                MessageBox box =
                    new MessageBox(
                        component, component.getSender().equals(this.currentUser.getName()));
                chatComponentBox.getChildren().add(box);
              });
    }

    this.chatContent.setVvalue(1);
    this.chatContent.setContent(chatComponentBox);

    final VBox chatTypeBox = new VBox();
    TextArea textArea = new TextArea();
    textArea.setWrapText(true);
    textArea.getStyleClass().add("type-area");
    textArea.setPadding(new Insets(0, 0, 0, 5));
    textArea.getStylesheets().add(getClass().getResource("/css/scroll_bar.css").toExternalForm());
    textArea.maxHeightProperty().bind(chatTypeBox.heightProperty().multiply(0.8));

    Button fileButton = new Button();
    fileButton.getStyleClass().add("file-button");
    try {
      Image image =
          new Image(
              Files.newInputStream(Paths.get(getClass().getResource("/image/file.png").toURI())));
      ImageView view = new ImageView(image);
      view.setFitHeight(25);
      view.setPreserveRatio(true);
      fileButton.setGraphic(view);
    } catch (Exception e) {
      if (MainApplication.debug()) {
        e.printStackTrace();
      }
    }
    fileButton.setOnAction(
        e -> {
          if (this.getRoom().getType() == RoomType.EMPTY) {
            return;
          }
          FileChooser chooser = new FileChooser();
          chooser.setTitle("选择文件");
          File choose = chooser.showOpenDialog(this.getScene().getWindow());
          if (choose == null) {
            return;
          }
          long length = choose.length();
          JSONObject object = new JSONObject();
          object.put("room", this.getRoom().getRoomID());
          object.put("length", length);
          object.put("name", choose.getName());
          object.put("user", this.currentUser.getName());
          long auth = System.nanoTime();
          object.put("auth", auth);
          Packet packet = new Packet(PacketType.MESSAGE, this.currentUser.getName(), 1, 1, object);
          try {
            this.controller.sendPacket(packet);
            Thread uploadThread =
                new Thread(
                    () -> {
                      Socket socket = new Socket();
                      try {
                        socket.connect(
                            new InetSocketAddress(
                                NetConfig.DEFAULT_SERVER_HOST, NetConfig.SERVER_DOWNLOAD_PORT),
                            10000);
                        BufferedWriter writer =
                            new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                        JSONObject object1 = new JSONObject();
                        object1.put("auth", auth);
                        Packet packet1 =
                            new Packet(
                                PacketType.FILE,
                                ChatSplitPane.this.currentUser.getName(),
                                1,
                                0,
                                object1);
                        PacketIO.sendPacket(writer, packet1);
                        Thread.sleep(50);
                        DataOutputStream outputStream =
                            new DataOutputStream(socket.getOutputStream());
                        DataInputStream input = new DataInputStream(new FileInputStream(choose));
                        byte[] bytes = new byte[1024];
                        while (input.read(bytes) != -1) {
                          outputStream.write(bytes);
                        }
                        outputStream.flush();
                        socket.close();
                      } catch (Exception ex) {
                        if (MainApplication.debug()) {
                          ex.printStackTrace();
                        }
                      }
                    });
            uploadThread.setDaemon(true);
            uploadThread.start();
          } catch (IOException ex) {
            if (MainApplication.debug()) {
              ex.printStackTrace();
            }
          }
        });

    JFXButton send = new JFXButton("发送");
    send.getStyleClass().add("send-button");
    send.setOnAction(
        e -> {
          if (textArea.getText().isEmpty()) {
            return;
          }
          if (this.getRoom().getType() == RoomType.EMPTY) {
            return;
          }
          JSONObject object = new JSONObject();
          object.put("room", this.getRoom().getRoomID());
          JSONObject content = new JSONObject();
          content.put("message", textArea.getText());
          object.put(
              "ci",
              new ChatInformation(
                  ChatInformationType.MESSAGE,
                  this.currentUser.getName(),
                  content,
                  new Date().getTime()));
          Packet messagePacket =
              new Packet(PacketType.MESSAGE, this.currentUser.getName(), 0, 0, object);
          try {
            this.controller.sendPacket(messagePacket);
          } catch (IOException ex) {
            if (MainApplication.debug()) {
              ex.printStackTrace();
            }
          }
          textArea.setText("");
        });

    send.setAlignment(Pos.CENTER);
    send.setPadding(new Insets(5, 10, 5, 10));
    VBox.setMargin(fileButton, new Insets(5, 0, 0, 5));
    VBox.setMargin(send, new Insets(10, 20, 10, 540));

    chatTypeBox.getChildren().addAll(fileButton, textArea, send);
    this.getItems().addAll(chatHeader, chatContent, chatTypeBox);
  }

  public void append(ChatInformation information) {
    if (this.getRoom().getType() == RoomType.EMPTY) {
      return;
    }
    this.history.append(information);
    MessageBox box =
        new MessageBox(information, information.getSender().equals(this.currentUser.getName()));

    boolean scroll = this.chatContent.getVvalue() > 0.8;
    this.chatComponentBox.getChildren().add(box);
    final KeyFrame kf =
        new KeyFrame(
            Duration.millis(32),
            e -> {
              if (scroll) {
                this.chatContent.setVvalue(1);
              }
            });
    final Timeline timeline = new Timeline(kf);
    Platform.runLater(timeline::play);
  }

  public ChatRoom getRoom() {
    return room;
  }
}
