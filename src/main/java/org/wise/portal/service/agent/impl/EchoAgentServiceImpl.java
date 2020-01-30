package org.wise.portal.service.agent.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.wise.portal.service.agent.EchoAgentService;
import org.wise.vle.domain.WebSocketMessage;

@Service
public class EchoAgentServiceImpl implements EchoAgentService, MessageListener {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        // student work {"serverSaveTime":1575938522175,"periodId":2,"componentType":"OpenResponse","clientSaveTime":1575938520000,"workgroupId":2,"componentId":"6o1m75beee","studentData":{"attachments":[],"submitCounter":0,"response":"My name is Tony!!"},"id":5,"runId":3,"isSubmit":false,"nodeId":"node1","isAutoSave":false}
        try {
            JSONObject messageJSON = new JSONObject(new String(message.getBody()));
            if (messageJSON.get("type").equals("studentWorkToClassroom")
                    || messageJSON.get("type").equals("studentWorkToTeacher")) {
                WebSocketMessage webSockeMessage = new WebSocketMessage(
                        "studentWork", messageJSON.getString("studentWork"));
                System.out.printf("student work %s",
                        messageJSON.get("studentWork"));
                simpMessagingTemplate.convertAndSend(messageJSON.getString("topic"),
                        webSockeMessage);
                JSONObject studentWork = messageJSON.getJSONObject("studentWork");
                String response = studentWork.getJSONObject("studentData").getString("response");
                String workgroupId = messageJSON.getJSONObject("studentWork").getString("workgroupId");
                String echoResponse = "Hello Little Friend RE:" + response;
                studentWork.put("echoResponse", echoResponse);
                WebSocketMessage echoWebSockeMessage = new WebSocketMessage(
                        "echoAgent", studentWork.toString());
                simpMessagingTemplate.convertAndSend("/topic/workgroup/" + workgroupId,
                    echoWebSockeMessage);
            } else if (messageJSON.get("type").equals("eventToAgent")) {
              JSONObject event = messageJSON.getJSONObject("event");
              if (event.getString("event").equals("hintRequested")) {
                String response = event.getJSONObject("data").getString("response");
                String workgroupId = event.getString("workgroupId");
                String echoResponse = "Hint Requested for:" + response;
                event.put("echoResponse", echoResponse);
                WebSocketMessage echoWebSockeMessage = new WebSocketMessage(
                        "echoAgent", event.toString());
                simpMessagingTemplate.convertAndSend("/topic/workgroup/" + workgroupId,
                    echoWebSockeMessage);
              }
            }
        } catch (MessagingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }



}
