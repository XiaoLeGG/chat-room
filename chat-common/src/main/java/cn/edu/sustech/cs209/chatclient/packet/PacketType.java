package cn.edu.sustech.cs209.chatclient.packet;

public enum PacketType {
  LOGIN,
  REGISTER,
  USER,
  CHAT_ROOM,
  MESSAGE,
  FILE;

  private PacketType() {}
}
