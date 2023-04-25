package cn.edu.sustech.cs209.chatclient.view;

import cn.edu.sustech.cs209.chatclient.MainApplication;
import cn.edu.sustech.cs209.chatclient.controller.ChatController;
import cn.edu.sustech.cs209.chatclient.model.ChatInformation;
import cn.edu.sustech.cs209.chatclient.model.ChatRoom;
import cn.edu.sustech.cs209.chatclient.model.ChatRoom.RoomType;
import cn.edu.sustech.cs209.chatclient.model.ChatRoomHistory;
import cn.edu.sustech.cs209.chatclient.model.UserPI;
import cn.edu.sustech.cs209.chatclient.packet.Packet;
import cn.edu.sustech.cs209.chatclient.packet.PacketType;
import com.alibaba.fastjson.JSONObject;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.effects.JFXDepthManager;
import com.jfoenix.validation.RequiredFieldValidator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javafx.animation.Animation;
import javafx.animation.FillTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

public class ChatPane {

  private ChatController chatController;
  private Stage stage;
  private Scene scene;
  private StackPane pane;
  private ChatSplitPane emptyChatBlock;
  private HBox mainBox;
  private HashMap<Integer, ChatSplitPane> chatBlockMap;
  private ListView<ChatRoom> chatRoomListView;
  private HashMap<Integer, Boolean> newMessageHint;

  public void showError(TextFlow flow) {
    ViewUtils.showError(this.pane, flow);
  }

  public void showInfo(TextFlow flow) {
    ViewUtils.showInformation(this.pane, flow);
  }

  public void showWarning(TextFlow flow) {
    ViewUtils.showWarning(this.pane, flow);
  }

  public void appendMessage(int roomId, ChatInformation info) {
    if (this.chatBlockMap.get(roomId) == null) {
      return;
    }
    ChatSplitPane roomPane = this.chatBlockMap.get(roomId);
    int id = -1;
    if (this.chatRoomListView.getSelectionModel().getSelectedItem() != null) {
      id = this.chatRoomListView.getSelectionModel().getSelectedItem().getRoomID();
    }
    this.chatRoomListView.getItems().remove(roomPane.getRoom());
    roomPane.append(info);
    this.chatRoomListView.getItems().add(0, roomPane.getRoom());
    if (id == roomId) {
      this.chatRoomListView.getSelectionModel().select(0);
    } else {
      this.newMessageHint.put(roomId, true);
      this.chatRoomListView.refresh();
    }
  }

  public ChatPane(ChatController chatController) {
    this.chatController = chatController;
    this.chatBlockMap = new HashMap<>();
    this.newMessageHint = new HashMap<>();
  }

  public void show() {
    this.stage.show();
  }

  public void hide() {
    this.stage.hide();
  }

  public void appendChatRoom(ChatRoom room, ChatRoomHistory history) {
    ChatSplitPane chatBlock = this.createChatContentPane(room, history);
    this.chatBlockMap.put(room.getRoomID(), chatBlock);
    this.chatRoomListView.getItems().add(0, room);
  }

  public ChatSplitPane createChatContentPane(ChatRoom room, ChatRoomHistory history) {
    return new ChatSplitPane(
        room,
        history,
        room.getType() == RoomType.EMPTY ? null : this.chatController.getUser().getUserPI(),
        this.chatController);
  }

  public void init() {
    this.stage = new Stage();

    this.pane = new StackPane();
    this.scene = new Scene(pane);
    this.stage.setScene(scene);
    this.pane.getStylesheets().add(getClass().getResource("/css/chat_pane.css").toExternalForm());
    this.pane.getStyleClass().add("main-background");

    this.mainBox = new HBox();
    VBox left = new VBox();
    this.mainBox.setSpacing(50);
    left.setSpacing(40);

    StackPane profileBlock = new StackPane();
    profileBlock.getStyleClass().add("profile-block");
    JFXDepthManager.setDepth(profileBlock, 3);
    Label userNameLabel = new Label(this.chatController.getUser().getUserName());
    userNameLabel.getStyleClass().add("user-name-label");
    Label userNameTag = new Label("用户名:");
    userNameTag.getStyleClass().add("user-name-tag");
    HBox labelBox = new HBox();
    labelBox.getChildren().addAll(userNameTag, userNameLabel);
    labelBox.setAlignment(Pos.CENTER_LEFT);
    labelBox.setSpacing(10);
    profileBlock.getChildren().add(labelBox);
    profileBlock.setAlignment(Pos.CENTER_LEFT);
    profileBlock.setPadding(new Insets(0, 0, 0, 25));

    SplitPane chatListBlock = new SplitPane();
    chatListBlock.setOrientation(Orientation.VERTICAL);
    chatListBlock.setDividerPositions(0.1);
    chatListBlock.getStyleClass().add("chat-list-block");
    JFXDepthManager.setDepth(chatListBlock, 3);
    JFXButton newChatButton = new JFXButton("新建会话");
    newChatButton.getStyleClass().add("new-chat-button");
    newChatButton.setOnAction(
        e -> {
          Stage subStage = new Stage();
          subStage.initModality(Modality.APPLICATION_MODAL);
          subStage.initOwner(this.stage);
          StackPane subPane = new StackPane();
          Scene subScene = new Scene(subPane, 400, 600);
          subStage.setScene(subScene);
          subPane
              .getStylesheets()
              .add(getClass().getResource("/css/chat_pane.css").toExternalForm());
          subPane.setAlignment(Pos.TOP_CENTER);
          subPane.setPadding(new Insets(10, 10, 10, 10));
          VBox vbox = new VBox();
          subPane.getChildren().add(vbox);
          vbox.setAlignment(Pos.TOP_LEFT);
          Label title = new Label("新建会话");
          title.getStyleClass().add("new-chat-title");

          HBox hbox = new HBox();
          hbox.setAlignment(Pos.CENTER_LEFT);
          Label tag = new Label("会话名称：");
          tag.getStyleClass().add("tag-label");
          JFXTextField name = new JFXTextField();
          name.setDisable(true);
          name.getValidators().add(new RequiredFieldValidator("会话名称不能为空"));
          hbox.getChildren().addAll(tag, name);
          hbox.setSpacing(5);

          List<String> users = new ArrayList<>();

          JFXButton createButton = new JFXButton("创建");

          ListView<UserPI> listView = new ListView<>();
          listView.getStyleClass().add("user-list-view");
          listView.setCellFactory(
              new Callback<>() {
                @Override
                public ListCell<UserPI> call(ListView<UserPI> param) {
                  return new ListCell<>() {
                    @Override
                    protected void updateItem(UserPI item, boolean empty) {
                      super.updateItem(item, empty);
                      if (empty || item == null) {
                        setGraphic(null);
                        return;
                      }
                      StackPane pane = new StackPane();
                      pane.setAlignment(Pos.CENTER);
                      Text label = new Text(item.getName());
                      label.setStyle("-fx-font-weight: bold");
                      Circle circle = new Circle(5);
                      FillTransition fillTransition =
                          new FillTransition(Duration.seconds(0.5), circle);
                      fillTransition.setCycleCount(Animation.INDEFINITE);
                      fillTransition.setAutoReverse(true);
                      if (chatController.isOnline(item.getName())) {
                        fillTransition.setFromValue(Color.web("#06C179", 0.3));
                        fillTransition.setToValue(Color.web("#06C179"));
                      } else {
                        fillTransition.setFromValue(Color.web("#8B0000", 0.3));
                        fillTransition.setToValue(Color.web("#8B0000"));
                      }
                      fillTransition.play();

                      JFXCheckBox checkBox = new JFXCheckBox();
                      checkBox.setOnAction(
                          e -> {
                            if (checkBox.isSelected()) {
                              if (users.contains(item.getName())) {
                                return;
                              }
                              users.add(item.getName());
                              if (users.size() > 1) {
                                createButton.setDisable(false);
                              }
                              if (users.size() > 2) {
                                name.setDisable(false);
                                if (name.getText() == null || name.getText().isEmpty()) {
                                  StringBuilder builder = new StringBuilder();
                                  users.stream()
                                      .sorted()
                                      .limit(3)
                                      .forEach(
                                          e3 -> {
                                            builder.append(e3).append(", ");
                                          });
                                  name.setText(builder.substring(0, builder.length() - 2));
                                }
                              }
                            } else {
                              if (!users.contains(item.getName())) {
                                return;
                              }
                              users.remove(item.getName());
                              if (users.size() < 2) {
                                createButton.setDisable(true);
                              }
                              if (users.size() < 3) {
                                name.setDisable(true);
                              }
                            }
                          });
                      if (item.getName().equals(chatController.getUser().getUserName())) {
                        checkBox.setSelected(true);
                        checkBox.setDisable(true);
                        if (!users.contains(item.getName())) {
                          users.add(item.getName());
                        }
                      }
                      pane.getChildren().addAll(label, circle, checkBox);
                      StackPane.setAlignment(label, Pos.CENTER_LEFT);
                      StackPane.setAlignment(circle, Pos.CENTER);
                      StackPane.setAlignment(checkBox, Pos.CENTER_RIGHT);

                      setGraphic(pane);
                    }
                  };
                }
              });
          listView.getItems().addAll(chatController.getUsers());
          listView.refresh();
          ScrollPane scrollPane = new ScrollPane();
          scrollPane.setContent(listView);
          scrollPane.getStyleClass().add("user-list-view-scroll-pane");
          scrollPane
              .getStylesheets()
              .add(getClass().getResource("/css/scroll_bar.css").toExternalForm());
          scrollPane.setFitToWidth(true);

          createButton.getStyleClass().add("create-button");
          createButton.setOnAction(
              e1 -> {
                JSONObject object = new JSONObject();

                if (users.size() == 2) {
                  object.put("type", RoomType.PRIVATE.name());
                  object.put("title", "private_room");
                  object.put("users", users);
                  boolean existed =
                      chatRoomListView.getItems().stream()
                          .anyMatch(
                              e2 -> {
                                if (e2.getType() == RoomType.PRIVATE
                                    && new HashSet<>(Arrays.asList(e2.getUsers()))
                                        .containsAll(users)) {
                                  chatRoomListView.getSelectionModel().select(e2);
                                  subStage.close();
                                  return true;
                                }
                                return false;
                              });
                  if (existed) {
                    return;
                  }
                } else {
                  if (users.size() > 2 && name.validate()) {
                    object.put("type", RoomType.GROUP.name());
                    object.put("title", name.getText());
                    object.put("users", users);
                  } else {
                    return;
                  }
                }
                Packet chatRoomCreate =
                    new Packet(
                        PacketType.CHAT_ROOM, chatController.getUser().getUserName(), 1, 0, object);
                try {
                  chatController.sendPacket(chatRoomCreate);
                } catch (IOException ex) {
                  if (MainApplication.debug()) {
                    ex.printStackTrace();
                  }
                  ViewUtils.showError(subPane, ViewUtils.generateTextFlow("创建失败"));
                  return;
                }
                subStage.close();
              });

          HBox subStageButtonBox = new HBox();
          subStageButtonBox.setAlignment(Pos.CENTER_RIGHT);
          subStageButtonBox.setPadding(new Insets(10));
          subStageButtonBox.getChildren().add(createButton);

          vbox.setSpacing(20);
          vbox.getChildren().addAll(title, hbox, scrollPane, subStageButtonBox);

          subStage.show();
        });

    ScrollPane roomListScroll = new ScrollPane();
    roomListScroll
        .getStylesheets()
        .add(getClass().getResource("/css/scroll_bar.css").toExternalForm());
    this.chatRoomListView = new ListView<>();
    this.chatRoomListView.getStyleClass().add("chat-room-list-view");
    this.chatRoomListView.maxHeightProperty().bind(roomListScroll.heightProperty());
    this.chatRoomListView
        .getStylesheets()
        .add(getClass().getResource("/css/scroll_bar.css").toExternalForm());
    this.chatRoomListView.setCellFactory(
        param ->
            new ListCell<>() {
              @Override
              protected void updateItem(ChatRoom room, boolean empty) {
                super.updateItem(room, empty);
                if (empty || room == null || room.getType() == RoomType.EMPTY) {
                  super.getStyleClass().add("chat-room-list-cell-empty");
                  setGraphic(null);
                  return;
                }
                HBox hbox = new HBox();
                hbox.setPadding(new Insets(10, 10, 10, 10));
                Label label = new Label();
                hbox.getChildren().add(label);
                hbox.setSpacing(20);
                if (room.getType() == RoomType.PRIVATE) {
                  String to;
                  if (room.getUsers()[0].equals(chatController.getUser().getUserName())) {
                    to = room.getUsers()[1];
                  } else {
                    to = room.getUsers()[0];
                  }
                  label.setText(to);
                  Circle circle = new Circle(5);
                  FillTransition fillTransition = new FillTransition(Duration.seconds(0.5), circle);
                  fillTransition.setCycleCount(Animation.INDEFINITE);
                  fillTransition.setAutoReverse(true);
                  if (chatController.isOnline(to)) {
                    fillTransition.setFromValue(Color.web("#06C179", 0.3));
                    fillTransition.setToValue(Color.web("#06C179"));
                  } else {
                    fillTransition.setFromValue(Color.web("#8B0000", 0.3));
                    fillTransition.setToValue(Color.web("#8B0000"));
                  }
                  fillTransition.play();
                  hbox.getChildren().add(circle);
                } else {
                  label.setText("群聊：" + room.getName());
                }
                label.getStyleClass().add("chat-room-list-view-label");
                if (newMessageHint.get(room.getRoomID()) != null
                    && newMessageHint.get(room.getRoomID())) {
                  Label newMessage = new Label("新消息");
                  newMessage.setPadding(new Insets(0, 10, 0, 10));
                  newMessage.getStyleClass().add("new-message-hint");
                  hbox.getChildren().add(newMessage);
                }
                hbox.setAlignment(Pos.CENTER_LEFT);
                hbox.setMaxHeight(35);
                hbox.setMinHeight(35);

                super.selectedProperty()
                    .addListener(
                        (observable, oldValue, newValue) -> {
                          if (newValue) {
                            if (newMessageHint.get(room.getRoomID()) != null
                                && newMessageHint.get(room.getRoomID())) {
                              newMessageHint.put(room.getRoomID(), false);
                              hbox.getChildren().remove(hbox.getChildren().size() - 1);
                            }
                            mainBox.getChildren().remove(1);
                            mainBox.getChildren().add(chatBlockMap.get(room.getRoomID()));
                          }
                        });
                super.getStyleClass().add("chat-room-list-cell");
                setGraphic(hbox);
              }
            });

    HBox buttonBox = new HBox();
    buttonBox.getChildren().add(newChatButton);
    buttonBox.setPadding(new Insets(10, 10, 10, 10));
    buttonBox.maxHeightProperty().bind(chatListBlock.heightProperty().multiply(0.1));
    roomListScroll.maxHeightProperty().bind(chatListBlock.heightProperty().multiply(0.8));
    roomListScroll.setContent(this.chatRoomListView);
    roomListScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    roomListScroll.setFitToWidth(true);
    chatListBlock.getItems().addAll(buttonBox, roomListScroll);

    left.getChildren().addAll(profileBlock, chatListBlock);
    this.emptyChatBlock =
        createChatContentPane(
            new ChatRoom("", 0, RoomType.EMPTY), new ChatRoomHistory(new ArrayList<>()));

    this.mainBox.getChildren().addAll(left, emptyChatBlock);

    left.setAlignment(Pos.CENTER);
    this.mainBox.setAlignment(Pos.CENTER);
    StackPane.setAlignment(this.mainBox, Pos.CENTER);
    this.pane.getChildren().add(this.mainBox);
    this.stage
        .focusedProperty()
        .addListener(
            e -> {
              if (this.stage.isFocused()) {
                newChatButton.setDisable(true);
                Platform.runLater(() -> newChatButton.setDisable(false));
              }
            });
  }

  public void refreshUserList() {
    this.chatRoomListView.refresh();
  }
}
