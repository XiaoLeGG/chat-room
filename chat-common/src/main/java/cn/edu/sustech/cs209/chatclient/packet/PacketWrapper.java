package cn.edu.sustech.cs209.chatclient.packet;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.nio.charset.StandardCharsets;

public class PacketWrapper {
	
	public static JSONObject wrapToJSONObject(Packet packet) {
		return (JSONObject) JSON.toJSON(packet);
	}
	
	public static String wrapToString(Packet packet) {
		return wrapToJSONObject(packet).toJSONString();
	}
	
	public static byte[] wrapToBytes(Packet packet) {
		return wrapToString(packet).getBytes(
			StandardCharsets.UTF_8);
	}
	
	public static Packet unwrap(byte[] bytes) {
		return unwrap(new String(bytes, StandardCharsets.UTF_8));
	}
	
	public static Packet unwrap(String json) {
		try {
			return JSON.parseObject(json, Packet.class);
		} catch (Exception e) {
			return null;
		}
		
	}
	
}
