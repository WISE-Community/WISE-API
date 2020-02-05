package org.wise.portal.score.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;

import javax.persistence.*;
import java.sql.Timestamp;

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

  @Column(name = "runId")
  private Long runId = null;

  @Column(name = "workgroupId")
  private Long workgroupId = null;

  @Column(name = "projectId")
  private Long projectId = null;

  @Column(name = "startTime")
  private Timestamp startTime;

  @Column(name = "endTime")
  private Timestamp endTime;

  @Column(name = "periodId")
  private Long periodId = null;

  @Column(name = "complete")
  private Boolean complete;

//  @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//  private List<TaskRequest> taskRequests = new ArrayList<>();

  public JSONObject toJSONObject() {
    JSONObject jsonObject = null;
    try {
      jsonObject = new JSONObject();
      jsonObject.put("id", getId());
      jsonObject.put("runId", getRunId());
      jsonObject.put("periodId", getPeriodId());
      jsonObject.put("workgroupId", getWorkgroupId());
      jsonObject.put("projectId", getProjectId());
      jsonObject.put("complete", getComplete());
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
