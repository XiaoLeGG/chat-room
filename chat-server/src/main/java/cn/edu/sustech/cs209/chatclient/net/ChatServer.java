package cn.edu.sustech.cs209.chatclient.net;

import cn.edu.sustech.cs209.chatclient.model.UserManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
	
	private ServerSocket serverSocket;
	private ExecutorService threadPool;
	private StringBuilder log;
	private UserManager manager;
	public ChatServer() {}
	
	public void initServer(String host, int port) throws IOException {
		log = new StringBuilder();
		manager = new UserManager(this);
		serverSocket = new ServerSocket();
		serverSocket.bind(new InetSocketAddress(host, port));
		serverSocket.setSoTimeout(7 * 24 * 60 * 60 * 1000);
		threadPool = Executors.newCachedThreadPool();
		this.info("Server started at " + host + ":" + port);
		
	}
	
	private boolean stop = false;
	
	public void startServer() throws IOException {
		stop = false;
		while (!stop) {
			Socket client = serverSocket.accept();
			this.info("Client connected from " + client.getInetAddress());
			threadPool.execute(new UserThread(client, this));
		}
	}
	
	public UserManager getUserManager() {
		return manager;
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
