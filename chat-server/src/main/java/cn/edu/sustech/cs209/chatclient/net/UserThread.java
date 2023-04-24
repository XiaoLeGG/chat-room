package cn.edu.sustech.cs209.chatclient.net;

import cn.edu.sustech.cs209.chatclient.model.ChatInformation;
import cn.edu.sustech.cs209.chatclient.model.ChatRoom;
import cn.edu.sustech.cs209.chatclient.model.ChatRoom.RoomType;
import cn.edu.sustech.cs209.chatclient.model.ChatRoomHistory;
import cn.edu.sustech.cs209.chatclient.model.User;
import cn.edu.sustech.cs209.chatclient.packet.Packet;
import cn.edu.sustech.cs209.chatclient.packet.PacketIO;
import cn.edu.sustech.cs209.chatclient.packet.PacketType;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class UserThread extends Thread {

  private Socket socket;
  private ChatServer server;
  private User user;
  private BufferedReader reader;
  private BufferedWriter writer;

  public UserThread(Socket socket, ChatServer server) {
    this.socket = socket;
    this.server = server;
  }

  public boolean isClosed() {
    return this.socket == null || this.socket.isClosed();
  }

  public void closeSocket() throws IOException {
    if (this.socket == null) {
      return;
    }
    if (!this.socket.isClosed()) {
      this.socket.close();
    }
  }

  public User getUser() {
    return this.user;
  }

  @Override
  public void run() {
    try {
      this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
      this.writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
      Packet packet = PacketIO.receivePacket(this.reader);
      if (packet == null) {
        this.socket.close();
        return;
      }
      if (packet.getType() == PacketType.LOGIN) {
        this.server.info("Receive login packet from " + socket.getInetAddress());
        JSONObject content = packet.getContent();
        if (!content.containsKey("username") || !content.containsKey("password")) {
          this.socket.close();
          return;
        }
        String username = content.getString("username");
        String password = content.getString("password");
        if (this.server.getUserManager().getOnlineUserThread(username) != null) {
          try {
            UserThread thread = this.server.getUserManager().getOnlineUserThread(username);
            thread.sendPacket(new Packet(PacketType.USER, "server", 0, 2, new JSONObject()));
            thread.closeSocket();
          } catch (Exception e) {
            if (this.server.debug()) {
              e.printStackTrace();
            }
          } finally {
            Thread.sleep(50);
          }
        }
        this.user = this.server.getUserManager().loginUser(username, password, this);
        if (user == null) {
          PacketIO.sendPacket(
              this.writer, new Packet(PacketType.LOGIN, "server", 0, 0, new JSONObject()));
          this.socket.close();
          return;
        } else {
          PacketIO.sendPacket(
              this.writer,
              new Packet(PacketType.LOGIN, "server", 0, 1, (JSONObject) JSON.toJSON(user)));
          PacketIO.sendPacket(this.writer, PacketUtils.createServerUsersListPacket(this.server));
          this.server.broadcastPacket(PacketUtils.createOnlineUsersListPacket(this.server));
          List<Integer> list = new ArrayList<>(user.getChatRooms());
          list.sort(
              (a, b) -> {
                ChatRoomHistory historyA = this.server.getChatRoomManager().getChatRoomHistory(a);
                ChatRoomHistory historyB = this.server.getChatRoomManager().getChatRoomHistory(b);
                long valueA =
                    historyA.getInformationList().isEmpty()
                        ? 0
                        : historyA
                            .getInformationList()
                            .get(historyA.getInformationList().size() - 1)
                            .getTimestamp();
                long valueB =
                    historyB.getInformationList().isEmpty()
                        ? 0
                        : historyB
                            .getInformationList()
                            .get(historyB.getInformationList().size() - 1)
                            .getTimestamp();
                return Long.compare(valueA, valueB);
              });
          for (int id : list) {
            ChatRoom room = this.server.getChatRoomManager().getChatRoom(id);
            if (room == null) {
              continue;
            }
            ChatRoomHistory history = this.server.getChatRoomManager().getChatRoomHistory(id);

            JSONObject object = new JSONObject();
            object.put("room", room);
            object.put("history", history);
            Packet historyPacket = new Packet(PacketType.CHAT_ROOM, "server", 0, 2, object);
            PacketIO.sendPacket(this.writer, historyPacket);
          }

          this.server.info("User " + user.getUserName() + " logged in");
          this.startReceive();
        }
      }
      if (packet.getType() == PacketType.REGISTER) {
        this.server.info("Receive register packet from " + socket.getInetAddress());
        JSONObject content = packet.getContent();
        if (!content.containsKey("username") || !content.containsKey("password")) {
          return;
        }
        String username = content.getString("username");
        String password = content.getString("password");
        User user = this.server.getUserManager().createUser(username, password);
        if (user == null) {
          PacketIO.sendPacket(
              this.writer, new Packet(PacketType.REGISTER, "server", 0, 0, new JSONObject()));
          this.socket.close();
          return;
        }
        if (user.getUserName() == null) {
          PacketIO.sendPacket(
              this.writer,
              new Packet(
                  PacketType.REGISTER, "server", 0, 1, JSON.parseObject("{\"msg\": \"用户名已存在\"}")));
          this.socket.close();
          return;
        }
        PacketIO.sendPacket(
            this.writer,
            new Packet(PacketType.REGISTER, "server", 0, 2, (JSONObject) JSON.toJSON(user)));
        this.socket.close();
        this.server.broadcastPacket(PacketUtils.createServerUsersListPacket(this.server));
        this.server.info("User " + user.getUserName() + " registered");
      }
      return;
    } catch (Exception e) {
      if (this.server.debug()) {
        e.printStackTrace();
      }
      return;
    }
  }

  private void startReceive() {
    try {
      while (true) {
        Packet packet = PacketIO.receivePacket(this.reader);
        if (packet != null) {
          if (packet.getType() == PacketType.MESSAGE) {
            this.server.info("Receive message packet from " + this.user.getUserName());
            if (packet.getSubCode() == 0) {
              ChatInformation ci =
                  JSON.parseObject(
                      packet.getContent().getJSONObject("ci").toJSONString(),
                      ChatInformation.class);
              ChatRoom chatRoom =
                  this.server
                      .getChatRoomManager()
                      .getChatRoom(packet.getContent().getInteger("room"));
              if (chatRoom == null) {
                continue;
              }

              Packet send = new Packet(PacketType.MESSAGE, "server", 0, 0, packet.getContent());
              for (String user : chatRoom.getUsers()) {
                UserThread thread = this.server.getUserManager().getOnlineUserThread(user);
                if (thread == null) {
                  continue;
                }
                thread.sendPacket(send);
              }
              this.server.getChatRoomManager().getChatRoomHistory(chatRoom.getRoomID()).append(ci);
            }
            if (packet.getSubCode() == 1) {
              this.server.newFileAuth(packet.getContent().getLong("auth"), packet.getContent());
            }
          }
          if (packet.getType() == PacketType.CHAT_ROOM) {
            this.server.info("Receive chat room packet from " + this.user.getUserName());
            if (packet.getSubCode() == 0) {
              JSONArray content = packet.getContent().getJSONArray("users");
              String[] users = new String[content.size()];
              users = (String[]) content.toArray(users);
              String title = packet.getContent().getString("title");
              RoomType type = RoomType.valueOf(packet.getContent().getString("type"));
              ChatRoom chatRoom =
                  this.server.getChatRoomManager().createChatRoom(title, type, users);
              if (chatRoom == null) {
                JSONObject object = new JSONObject();
                object.put("msg", "创建失败");
                Packet send = new Packet(PacketType.CHAT_ROOM, "server", 0, 0, object);
                this.sendPacket(send);
                continue;
              }
              JSONObject object = new JSONObject();
              object.put("room", JSON.toJSON(chatRoom));
              object.put("history", JSON.toJSON(new ChatRoomHistory(new ArrayList<>())));
              Packet send = new Packet(PacketType.CHAT_ROOM, "server", 0, 1, object);
              for (String user : chatRoom.getUsers()) {
                User rawUser = this.server.getUserManager().getUser(user);
                rawUser.addChatRoom(chatRoom.getRoomID());
                this.server.getUserManager().saveUser(rawUser);
                UserThread u = this.server.getUserManager().getOnlineUserThread(user);
                if (u != null) {
                  u.sendPacket(send);
                }
              }
            }
          }
        }
      }
    } catch (Exception e) {
      if (this.server.debug()) {
        e.printStackTrace();
      }
      server.info(
          "User "
              + this.user.getUserName()
              + "("
              + this.socket.getInetAddress().getHostAddress()
              + ") disconnected...");
      this.server.getUserManager().logoutUser(this.user.getUserName());
      this.server.broadcastPacket(PacketUtils.createOnlineUsersListPacket(this.server));
      return;
    }
  }

  public boolean sendPacket(Packet packet) throws IOException {
    if (this.socket == null || this.socket.isClosed()) {
      return false;
    }
    PacketIO.sendPacket(this.writer, packet);
    return true;
  }
}
