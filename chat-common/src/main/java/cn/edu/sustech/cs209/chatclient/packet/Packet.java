package cn.edu.sustech.cs209.chatclient.packet;

import com.alibaba.fastjson.JSONObject;

public class Packet {

  private PacketType type;
  private String sender;
  private int authorizationCode;
  private int subCode;
  private JSONObject content;

  public Packet(
      PacketType type, String sender, int authorizationCode, int subCode, JSONObject content) {
    this.type = type;
    this.sender = sender;
    this.authorizationCode = authorizationCode;
    this.subCode = subCode;
    this.content = content;
  }

  public PacketType getType() {
    return type;
  }

  public String getSender() {
    return sender;
  }

  public int getAuthorizationCode() {
    return authorizationCode;
  }

  public int getSubCode() {
    return subCode;
  }

  public JSONObject getContent() {
    return content;
  }
}
