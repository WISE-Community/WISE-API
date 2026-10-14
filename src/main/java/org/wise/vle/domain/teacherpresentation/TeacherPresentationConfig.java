package org.wise.vle.domain.teacherpresentation;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.group.impl.PersistentGroup;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.domain.workgroup.impl.WorkgroupImpl;

/**
 * A teacher's TeacherPresentation configuration for one component in one period of a run. It
 * stores which student work items (e.g. discussion posts and comments) the teacher chose to
 * present, how student names are displayed, and an optional override of the default prompt.
 */
@Entity(name = "teacherPresentationConfig")
@Table(name = "teacher_presentation_config",
    uniqueConstraints = @UniqueConstraint(name = "teacherPresentationConfigUniqueKey",
        columnNames = { "runId", "periodId", "nodeId", "componentId" }),
    indexes = @Index(columnList = "runId", name = "teacherPresentationConfigRunIdIndex"))
@Getter
@Setter
public class TeacherPresentationConfig {

  public static final String NAMES_SHOW = "show";
  public static final String NAMES_HIDE = "hide";
  public static final String NAMES_ANONYMIZE = "anonymize";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(targetEntity = RunImpl.class, fetch = FetchType.LAZY)
  @JoinColumn(name = "runId", nullable = false)
  @JsonIgnore
  private Run run;

  @ManyToOne(targetEntity = PersistentGroup.class, fetch = FetchType.LAZY)
  @JoinColumn(name = "periodId", nullable = false)
  @JsonIgnore
  private Group period;

  @Column(name = "nodeId", length = 30, nullable = false)
  private String nodeId;

  @Column(name = "componentId", length = 30, nullable = false)
  private String componentId;

  @Column(name = "componentType", length = 30, nullable = false)
  private String componentType;

  /** JSON array of the form [{"studentWorkId": 123}, ...] */
  @Column(name = "items", length = 65536, columnDefinition = "text", nullable = false)
  private String items = "[]";

  @Column(name = "studentNamesDisplay", length = 30, nullable = false)
  private String studentNamesDisplay = NAMES_HIDE;

  /** The teacher's edited prompt. null means use the component's default prompt. */
  @Column(name = "prompt", length = 65536, columnDefinition = "text")
  private String prompt;

  @ManyToOne(targetEntity = WorkgroupImpl.class, fetch = FetchType.LAZY)
  @JoinColumn(name = "updatedByWorkgroupId")
  @JsonIgnore
  private Workgroup updatedByWorkgroup;

  @Column(name = "createdAt", nullable = false)
  private Timestamp createdAt;

  @Column(name = "updatedAt", nullable = false)
  private Timestamp updatedAt;
}
