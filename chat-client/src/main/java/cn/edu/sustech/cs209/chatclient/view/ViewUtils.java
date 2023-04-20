package cn.edu.sustech.cs209.chatclient.view;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialog.DialogTransition;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class ViewUtils {
	
	public static TextFlow generateTextFlow(String text) {
		TextFlow flow = new TextFlow();
		flow.getChildren().add(new Text(text));
		return flow;
	}
	
	public static void showWarning(StackPane pane, TextFlow flow) {
		JFXDialog dialog = new JFXDialog();
		dialog.getStylesheets().add(ViewUtils.class.getResource("/css/dialog.css").toExternalForm());
		dialog.setTransitionType(DialogTransition.CENTER);
		JFXDialogLayout layout = new JFXDialogLayout();
		Label label = new Label("警告");
		label.getStyleClass().add("warning-title");
		flow.getStyleClass().add("text");
		layout.setHeading(label);
		layout.setBody(flow);
		JFXButton close = new JFXButton("关闭");
		layout.setActions(close);
		close.setOnAction(e -> {
			dialog.close();
		});
		dialog.setContent(layout);
		dialog.show(pane);
	}
	
	public static void showError(StackPane pane, TextFlow flow) {
		JFXDialog dialog = new JFXDialog();
		dialog.getStylesheets().add(ViewUtils.class.getResource("/css/dialog.css").toExternalForm());
		dialog.setTransitionType(DialogTransition.CENTER);
		JFXDialogLayout layout = new JFXDialogLayout();
		Label label = new Label("错误");
		label.getStyleClass().add("error-title");
		flow.getStyleClass().add("text");
		layout.setHeading(label);
		layout.setBody(flow);
		JFXButton close = new JFXButton("关闭");
		close.setOnAction(e -> {
			dialog.close();
		});
		layout.setActions(close);
		dialog.setContent(layout);
		dialog.show(pane);
	}
	
	public static void showInfomation(StackPane pane, TextFlow flow) {
		JFXDialog dialog = new JFXDialog();
		dialog.getStylesheets().add(ViewUtils.class.getResource("/css/dialog.css").toExternalForm());
		dialog.setTransitionType(DialogTransition.CENTER);
		JFXDialogLayout layout = new JFXDialogLayout();
		Label label = new Label("提示");
		label.getStyleClass().add("info-title");
		flow.getStyleClass().add("text");
		layout.setHeading(label);
		layout.setBody(flow);
		JFXButton close = new JFXButton("关闭");
		layout.setActions(close);
		close.setOnAction(e -> {
			dialog.close();
		});
		dialog.setContent(layout);
		dialog.show(pane);
	}
	
}
