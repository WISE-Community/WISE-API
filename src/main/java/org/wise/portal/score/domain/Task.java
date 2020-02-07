package org.wise.portal.score.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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
  private String workgroupName;
  private Long projectId;
  private Long periodId;
  private Timestamp startTime;
  private Timestamp endTime;
  private Boolean complete;

  @JsonManagedReference
  @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<TaskRequest> taskRequests = new ArrayList<>();

  public JSONObject toJSONObject() {
    JSONObject jsonObject = null;
    try {
      jsonObject = new JSONObject();
      jsonObject.put("id", getId());
      jsonObject.put("runId", getRunId());
      jsonObject.put("periodId", getPeriodId());
      jsonObject.put("workgroupId", getWorkgroupId());
      jsonObject.put("workgroupName", getWorkgroupName());
      jsonObject.put("projectId", getProjectId());
      jsonObject.put("startTime", getStartTime());
      jsonObject.put("endTime", getEndTime());
      jsonObject.put("complete", getComplete());
      jsonObject.put("taskRequests", getTaskRequests());
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
