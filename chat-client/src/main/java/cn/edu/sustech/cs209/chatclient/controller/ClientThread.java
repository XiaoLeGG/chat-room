package cn.edu.sustech.cs209.chatclient.controller;

import cn.edu.sustech.cs209.chatclient.MainApplication;
import cn.edu.sustech.cs209.chatclient.net.ClientConnector;
import cn.edu.sustech.cs209.chatclient.packet.Packet;

public class ClientThread extends Thread {

  private ChatController controller;
  private ClientConnector connector;

  public ClientThread(ChatController controller, ClientConnector connector) {
    this.controller = controller;
    this.connector = connector;
  }

  @Override
  public void run() {
    try {
      while (true) {
        Packet packet = this.connector.accpetPacket();
        if (packet != null) {
          this.controller.handlePacket(packet);
        }
      }
    } catch (Exception e) {
      if (MainApplication.debug()) {
        e.printStackTrace();
      }
      this.controller.shutdown();
    }
  }
}
