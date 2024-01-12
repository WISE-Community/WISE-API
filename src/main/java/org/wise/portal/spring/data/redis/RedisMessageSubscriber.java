package org.wise.portal.spring.data.redis;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.wise.vle.domain.WebSocketMessage;

@Service
public class RedisMessageSubscriber implements MessageListener {

  @Autowired
  private SimpMessagingTemplate simpMessagingTemplate;

  public void onMessage(Message message, byte[] pattern) {
    try {
      JSONObject messageJSON = new JSONObject(new String(message.getBody()));
      switch (messageJSON.getString("type")) {
      case "currentAuthors":
        simpMessagingTemplate.convertAndSend(messageJSON.getString("topic"),
            messageJSON.getJSONArray("currentAuthors").toString());
        break;
      case "studentWorkToClassroom":
      case "studentWorkToTeacher":
        createAndSendWebSocketMessage("studentWork", messageJSON, "studentWork");
        break;
      case "annotationToTeacher":
        createAndSendWebSocketMessage("annotation", messageJSON, "annotation");
        break;
      case "studentStatusToTeacher":
        createAndSendWebSocketMessage("studentStatus", messageJSON, "studentStatus");
        break;
      case "achievementToTeacher":
        createAndSendWebSocketMessage("newStudentAchievement", messageJSON, "achievement");
        break;
      case "annotationToStudent":
        createAndSendWebSocketMessage("annotation", messageJSON, "annotation");
        break;
      case "notification":
        createAndSendWebSocketMessage("notification", messageJSON, "notification");
        break;
      case "pause":
        createAndSendWebSocketMessage("pause", messageJSON);
        break;
      case "unpause":
        createAndSendWebSocketMessage("unpause", messageJSON);
        break;
      case "node":
        createAndSendWebSocketMessage("node", messageJSON, "node");
        break;
      case "tagsToWorkgroup":
        createAndSendWebSocketMessage("tagsToWorkgroup", messageJSON, "tags");
        break;
      case "classmateStudentWork":
        createAndSendWebSocketMessage("classmateStudentWork", messageJSON, "studentWork");
        break;
      case "newWorkgroupJoinedRun":
        createAndSendWebSocketMessage("newWorkgroupJoinedRun", messageJSON);
        break;
      case "isTyping":
        createAndSendWebSocketMessage("isTyping", messageJSON, "body");
        break;
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  private void createAndSendWebSocketMessage(String type, JSONObject messageJSON,
      String contentField) throws JSONException {
    WebSocketMessage webSocketMessage = new WebSocketMessage(type,
        messageJSON.getString(contentField));
    sendWebSocketMessage(messageJSON, webSocketMessage);
  }

  private void createAndSendWebSocketMessage(String type, JSONObject messageJSON)
      throws JSONException {
    WebSocketMessage webSocketMessage = new WebSocketMessage(type, "");
    sendWebSocketMessage(messageJSON, webSocketMessage);
  }

  private void sendWebSocketMessage(JSONObject messageJSON, WebSocketMessage webSocketMessage)
      throws JSONException {
    simpMessagingTemplate.convertAndSend(messageJSON.getString("topic"), webSocketMessage);
  }
}
