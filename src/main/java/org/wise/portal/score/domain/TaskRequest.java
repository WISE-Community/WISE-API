package org.wise.portal.score.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.wise.vle.domain.PersistableDomain;

import javax.persistence.*;
import java.io.Serializable;

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
  private String name;

  @Column(name = "runId")
  private Long runId = null;

  @Column(name = "workgroupId")
  private Long workgroupId = null;

  @Column(name = "projectId")
  private Long projectId = null;

  @Column(name = "status")
  private String status = null;



//  @ManyToOne(fetch = FetchType.EAGER)
//  @JoinColumn(name = "tasks_id")
//  private Task task;


  @Override
  protected Class<?> getObjectClass() {
    return TaskRequest.class;
  }


}
