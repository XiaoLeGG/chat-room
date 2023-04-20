module cn.edu.sustech.cs209.chatclient {
	requires org.burningwave.core;
	requires javafx.controls;
	requires javafx.fxml;
	requires chat.common;
	
	requires org.kordamp.ikonli.javafx;
	requires com.jfoenix;
	requires java.base;
	requires fastjson;
	
	opens cn.edu.sustech.cs209.chatclient to javafx.fxml;
	exports cn.edu.sustech.cs209.chatclient;
}