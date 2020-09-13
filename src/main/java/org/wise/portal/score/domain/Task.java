package org.wise.portal.score.domain;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import org.json.JSONException;
import org.json.JSONObject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Each Task has one or more TaskRequests (Approved,Need Help)
 * Each task is associated with an activity
 *
 * @author Anthony Perritano
 */
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tasks")
public class Task {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  private String name;
  private Long runId;
  private Long workgroupId;
  private String activityId;
  private Long projectId;
  private Long periodId;
  private String periodName;
  private Integer duration;
  private Timestamp startTime;
  private Timestamp endTime;
  private String username;
  private Boolean complete = false;
  private Boolean started = false;
  private Boolean active = false;

  @JsonManagedReference
  @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<TaskRequest> taskRequests = new ArrayList<>();

  public void removeTaskRequest(TaskRequest taskRequest) {
    this.taskRequests.removeIf(u -> u.getId().equals(taskRequest.getId()));
  }

  public void addTaskRequest(TaskRequest taskRequest) {
    if (taskRequest != null) {
      if (this.taskRequests == null) {
        this.taskRequests = new ArrayList<>();
      }
      this.taskRequests.add(taskRequest);
    }
  }

  public JSONObject toJSONObject() {
    JSONObject jsonObject = null;
    try {
      jsonObject = new JSONObject();
      jsonObject.put("id", getId());
      jsonObject.put("runId", getRunId());
      jsonObject.put("periodId", getPeriodId());
      jsonObject.put("periodName", getPeriodName());
      jsonObject.put("workgroupId", getWorkgroupId());
      jsonObject.put("activityName", getName());
      jsonObject.put("activityId", getActivityId());
      jsonObject.put("projectId", getProjectId());
      jsonObject.put("startTime", getStartTime());
      jsonObject.put("endTime", getEndTime());
      jsonObject.put("complete", getComplete());
      jsonObject.put("active", getComplete());
      jsonObject.put("started", getStartTime());
      jsonObject.put("duration", getDuration());
      jsonObject.put("taskRequests", getTaskRequests());
      jsonObject.put("username", getUsername());
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return jsonObject;
  }

  public String toJSONString() {
    String jsonString = null;
    JSONObject jsonObject = toJSONObject();
    try {
      if (jsonObject != null) {
        jsonString = jsonObject.toString(3);
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return jsonString;
  }
}
