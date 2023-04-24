package cn.edu.sustech.cs209.chatclient.net;

import cn.edu.sustech.cs209.chatclient.packet.Packet;
import cn.edu.sustech.cs209.chatclient.packet.PacketIO;
import cn.edu.sustech.cs209.chatclient.packet.PacketType;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class PacketUtils {

  protected static Packet createServerUsersListPacket(ChatServer server) {
    JSONArray array = (JSONArray) JSON.toJSON(server.getUserManager().getUserPIs());
    JSONObject userContent = new JSONObject();
    userContent.put("users", array);
    return new Packet(PacketType.USER, "server", 0, 0, userContent);
  }

  protected static Packet createOnlineUsersListPacket(ChatServer server) {
    JSONObject content = new JSONObject();
    content.put("online", (JSONArray) JSON.toJSON(server.getUserManager().getOnlineUserPIs()));
    return new Packet(PacketType.USER, "server", 0, 1, content);
  }
}
