package cn.edu.sustech.cs209.chatclient.net;

import cn.edu.sustech.cs209.chatclient.model.ChatInformation;
import cn.edu.sustech.cs209.chatclient.model.ChatInformation.ChatInformationType;
import cn.edu.sustech.cs209.chatclient.model.ChatRoom;
import cn.edu.sustech.cs209.chatclient.model.ChatRoomManager;
import cn.edu.sustech.cs209.chatclient.model.UserManager;
import cn.edu.sustech.cs209.chatclient.packet.Packet;
import cn.edu.sustech.cs209.chatclient.packet.PacketIO;
import cn.edu.sustech.cs209.chatclient.packet.PacketType;
import com.alibaba.fastjson.JSONObject;
import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
	
	private ServerSocket serverSocket;
	private ServerSocket fileSocket;
	private ExecutorService threadPool;
	private ConcurrentHashMap<Long, JSONObject> fileAuth;
	
	private StringBuilder log;
	private UserManager manager;
	private ChatRoomManager roomManager;
	public ChatServer() {}
	
	public void initServer(String host, int port) throws IOException {
		log = new StringBuilder();
		this.manager = new UserManager(this);
		this.manager.init();
		this.roomManager = new ChatRoomManager(this);
		this.roomManager.init();
		serverSocket = new ServerSocket();
		serverSocket.bind(new InetSocketAddress(host, port));
		serverSocket.setSoTimeout(7 * 24 * 60 * 60 * 1000);
		fileSocket = new ServerSocket();
		fileSocket.bind(new InetSocketAddress(host, port + 1));
		fileSocket.setSoTimeout(7 * 24 * 60 * 60 * 1000);
		threadPool = Executors.newCachedThreadPool();
		fileAuth = new ConcurrentHashMap<>();
		this.info("Server started at " + host + ":" + port);
		
	}
	
	protected void newFileAuth(long auth, JSONObject content) {
		this.fileAuth.put(auth, content);
	}
	
	private boolean stop = false;
	
	public void startServer() throws IOException {
		stop = false;
		Thread fileThread = new Thread() {
			@Override
			public void run() {
				File dir = new File("files");
				if (!dir.exists()) {
					dir.mkdir();
				}
				ExecutorService filePool = Executors.newCachedThreadPool();
				while (!stop) {
					try {
						Socket client = fileSocket.accept();
						filePool.execute(() -> {
							try {
								BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
								Packet packet = PacketIO.receivePacket(reader);
								if (packet.getSubCode() == 0) {
									JSONObject object = packet.getContent();
									long auth = object.getLong("auth");
									JSONObject authObject = fileAuth.get(auth);
									if (authObject == null) {
										return;
									}
									if (!packet.getSender().equals(authObject.getString("user"))) {
										return;
									}
									DataInputStream input = new DataInputStream(client.getInputStream());
									String uuid = UUID.randomUUID().toString();
									File file = new File(dir, uuid);
									if (!file.exists()) {
										file.createNewFile();
									}
									FileOutputStream output = new FileOutputStream(file);
									byte[] bytes = new byte[1024];
									while (input.read(bytes) != -1) {
										output.write(bytes);
									}
									output.flush();;
									output.close();;
									fileAuth.remove(auth);
									ChatRoom room = roomManager.getChatRoom(authObject.getInteger("room"));
									JSONObject info = new JSONObject();
									info.put("file", authObject.getString("name"));
									info.put("remote-file", uuid);
									info.put("length", authObject.getLong("length"));
									ChatInformation ci = new ChatInformation(ChatInformationType.FILE, packet.getSender(), info, new Date().getTime());
									JSONObject send = new JSONObject();
									send.put("ci", ci);
									send.put("room", room.getRoomID());
									ChatServer.this.getChatRoomManager().getChatRoomHistory(room.getRoomID()).append(ci);
									ChatServer.this.getChatRoomManager().saveChatRoom(room.getRoomID());
									Packet sendPacket = new Packet(PacketType.MESSAGE, "server", 0, 1, send);
									for (String user : room.getUsers()) {
										UserThread userThread = manager.getOnlineUserThread(user);
										if (userThread != null) {
											userThread.sendPacket(sendPacket);
										}
									}
									return;
								}
								if (packet.getSubCode() == 1) {
									String name = packet.getContent().getString("name");
									File file = new File(dir, name);
									if (!file.exists()) {
										client.getOutputStream().close();
									}
									DataInputStream input = new DataInputStream(new FileInputStream(file));
									DataOutputStream stream = new DataOutputStream(client.getOutputStream());
									byte[] bytes = new byte[1024];
									while (input.read(bytes) != -1) {
										stream.write(bytes);
									}
									stream.flush();
									stream.close();
								}
							} catch (IOException e) {
								if (ChatServer.this.debug()) {
									e.printStackTrace();
								}
								return;
							}
						});
					} catch (Exception e) {
						if (ChatServer.this.debug()) {
							e.printStackTrace();
						}
					}
				}
			}
		};
		fileThread.setDaemon(true);
		fileThread.start();
		while (!stop) {
			Socket client = serverSocket.accept();
			this.info("Client connected from " + client.getInetAddress());
			threadPool.execute(new UserThread(client, this));
		}
	}
	
	public UserManager getUserManager() {
		return manager;
	}
	public ChatRoomManager getChatRoomManager() {
		return roomManager;
	}
	
	public void broadcastPacket(Packet packet) {
		this.manager.getOnlineUserThreads().stream().forEach((thread) -> {
			try {
				thread.sendPacket(packet);
			} catch (IOException e) {
				if (this.debug()) {
					e.printStackTrace();
				}
				this.error("Failed to send packet to " + thread.getUser().getUserName());
			}
		});
	}
	
	public void log(String msg) {
		String logMsg = "[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]" + msg;
		log.append(logMsg + "\n");
		
		System.out.println(logMsg);
	}
	
	public void info(String msg) {
		log("[INFO] " + msg);
	}
	
	public void error(String msg) {
		log("[ERROR] " + msg);
	}
	
	public void warning(String msg) {
		log("[WARNING] " + msg);
	}
	
	public void shutdownServer() {
		stop = true;
	}
	
	public boolean debug() {
		return true;
	}
	
}
