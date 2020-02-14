package org.wise.portal.score.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;
import org.wise.vle.domain.PersistableDomain;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Each Task as one or more TaskRequests (Approved,Need Help)
 *
 * @author Anthony Perritano
 */
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "task_requests")
public class TaskRequest extends PersistableDomain implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  private Long runId;
  private Long workgroupId ;
  private Long periodId;
  private Long projectId;
  private String status;
  private Boolean complete = false;
  private Timestamp startTime;
  private Timestamp endTime;


  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "tasks_id")
  @JsonIgnore
  private Task task;


  @Override
  protected Class<?> getObjectClass() {
    return TaskRequest.class;
  }
  public JSONObject toJSONObject() {
    JSONObject jsonObject = null;
    try {
      jsonObject = new JSONObject();
      jsonObject.put("id", getId());
      jsonObject.put("runId", getRunId());
      jsonObject.put("periodId", getPeriodId());
      jsonObject.put("projectId", getProjectId());
      jsonObject.put("workgroupId", getWorkgroupId());
      jsonObject.put("startTime", getStartTime());
      jsonObject.put("endTime", getEndTime());
      jsonObject.put("status", getStatus());
      jsonObject.put("complete", getStatus());
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
