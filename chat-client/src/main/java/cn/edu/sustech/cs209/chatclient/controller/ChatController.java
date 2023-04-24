package cn.edu.sustech.cs209.chatclient.controller;

import cn.edu.sustech.cs209.chatclient.model.ChatInformation;
import cn.edu.sustech.cs209.chatclient.model.ChatRoom;
import cn.edu.sustech.cs209.chatclient.model.ChatRoomHistory;
import cn.edu.sustech.cs209.chatclient.model.User;
import cn.edu.sustech.cs209.chatclient.model.UserPI;
import cn.edu.sustech.cs209.chatclient.net.ClientConnector;
import cn.edu.sustech.cs209.chatclient.packet.Packet;
import cn.edu.sustech.cs209.chatclient.packet.PacketType;
import cn.edu.sustech.cs209.chatclient.view.ChatPane;
import cn.edu.sustech.cs209.chatclient.view.ViewUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javafx.application.Platform;

public class ChatController {
  private LoginController loginController;
  private User user;
  private List<UserPI> userPIs;
  private List<String> onlineUsers;
  private ChatPane pane;
  private ClientConnector connector;
  private ClientThread thread;

  public ChatController(LoginController loginController, ClientConnector connector, User user) {
    this.loginController = loginController;
    this.connector = connector;
    this.user = user;
  }

  protected void startClientThread() {
    this.thread = new ClientThread(this, this.connector);
    this.thread.setDaemon(true);
    this.thread.start();
  }

  protected void shutdown() {
    this.shutdown("与服务器断开连接");
  }

  protected void shutdown(String errorMsg) {
    Platform.runLater(
        () -> {
          this.pane.hide();
          this.loginController.showLoginPane();
          this.loginController.showError(ViewUtils.generateTextFlow(errorMsg));
        });
  }

  public void handlePacket(Packet packet) {
    // Notice JFX thread
    // System.out.println(packet.getContent().toJSONString());
    if (packet.getType() == PacketType.USER) {
      if (packet.getSubCode() == 0) {
        JSONArray array = packet.getContent().getJSONArray("users");
        JSONObject[] userArray = new JSONObject[array.size()];
        array.toArray(userArray);
        this.userPIs = new ArrayList<>();
        for (JSONObject object : userArray) {
          UserPI userPI = (UserPI) JSON.parseObject(object.toJSONString(), UserPI.class);
          this.userPIs.add(userPI);
        }
      }
      if (packet.getSubCode() == 1) {
        JSONArray array = packet.getContent().getJSONArray("online");
        String[] users = new String[array.size()];
        array.toArray(users);
        this.onlineUsers = new ArrayList<>();
        onlineUsers.addAll(Arrays.asList(users));
      }
      if (packet.getSubCode() == 2) {
        this.shutdown("用户在其它地方登录");
      }
    }
    if (packet.getType() == PacketType.CHAT_ROOM) {
      if (packet.getSubCode() == 0) {
        Platform.runLater(
            () -> {
              this.pane.showError(ViewUtils.generateTextFlow(packet.getContent().getString("msg")));
            });
      }
      if (packet.getSubCode() == 1) {
        ChatRoom cr = JSON.parseObject(packet.getContent().get("room").toString(), ChatRoom.class);
        ChatRoomHistory history =
            JSON.parseObject(packet.getContent().get("history").toString(), ChatRoomHistory.class);
        Platform.runLater(
            () -> {
              this.pane.appendChatRoom(cr, history);
              this.pane.showInfo(ViewUtils.generateTextFlow("创建会话成功"));
            });

        user.addChatRoom(cr.getRoomID());
      }
      if (packet.getSubCode() == 2) {
        ChatRoom cr = JSON.parseObject(packet.getContent().get("room").toString(), ChatRoom.class);
        ChatRoomHistory history =
            JSON.parseObject(packet.getContent().get("history").toString(), ChatRoomHistory.class);
        Platform.runLater(() -> this.pane.appendChatRoom(cr, history));
      }
    }
    if (packet.getType() == PacketType.MESSAGE) {
      if (packet.getSubCode() == 0) {
        int id = packet.getContent().getInteger("room");
        ChatInformation information =
            JSON.parseObject(packet.getContent().get("ci").toString(), ChatInformation.class);
        Platform.runLater(
            () -> {
              this.pane.appendMessage(id, information);
            });
      }
      if (packet.getSubCode() == 1) {
        int id = packet.getContent().getInteger("room");
        ChatInformation information =
            JSON.parseObject(packet.getContent().get("ci").toString(), ChatInformation.class);
        Platform.runLater(
            () -> {
              this.pane.appendMessage(id, information);
            });
      }
    }
  }

  public boolean isOnline(String userName) {
    return this.onlineUsers.contains(userName);
  }

  public Collection<UserPI> getUsers() {
    return this.userPIs;
  }

  public User getUser() {
    return this.user;
  }

  public void showChatPane() {
    this.pane = new ChatPane(this);
    this.pane.init();
    this.pane.show();
    this.loginController.hideLoginPane();
  }

  public void sendPacket(Packet packet) throws IOException {
    this.connector.sendPacket(packet);
  }
}
