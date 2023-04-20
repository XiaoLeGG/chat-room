package cn.edu.sustech.cs209.chatclient.packet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class PacketIO {
	
	public static void sendPacket(OutputStream output, Packet packet) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));
		String json = PacketWrapper.wrapToString(packet);
		writer.write(json.length());
		writer.write(json);
		writer.flush();
	}
	
	public static Packet receivePacket(InputStream input) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
		int charLenght = reader.read();
		char[] read = new char[charLenght];
		reader.read(read);
		String packetString = new String(read);
		Packet packet = PacketWrapper.unwrap(packetString);
		return packet;
	}
	
}
