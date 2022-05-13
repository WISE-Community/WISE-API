package org.wise.portal.service.work.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wise.portal.service.work.BroadcastStudentWorkService;
import org.wise.portal.spring.data.redis.MessagePublisher;
import org.wise.vle.domain.work.StudentWork;

@Service
public class BroadcastStudentWorkServiceImpl implements BroadcastStudentWorkService {

  @Autowired
  private MessagePublisher redisPublisher;

  public void broadcastToClassroom(StudentWork studentWork) {
    try {
      JSONObject message = new JSONObject();
      message.put("type", "studentWorkToClassroom");
      message.put("topic", String.format("/topic/classroom/%s/%s", studentWork.getRun().getId(),
          studentWork.getPeriod().getId()));
      message.put("studentWork", studentWork.toJSON());
      redisPublisher.publish(message.toString());
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void broadcastToTeacher(StudentWork studentWork) {
    try {
      JSONObject message = new JSONObject();
      message.put("type", "studentWorkToTeacher");
      message.put("topic", String.format("/topic/teacher/%s", studentWork.getRun().getId()));
      message.put("studentWork", studentWork.toJSON());
      redisPublisher.publish(message.toString());
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

}
