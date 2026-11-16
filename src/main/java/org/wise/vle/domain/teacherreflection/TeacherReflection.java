package org.wise.vle.domain.teacherreflection;

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
 * A teacher's reflection and answer for a component in a period of a run.
 * Stored in teacher_reflections.
 */
@Entity(name = "teacherReflection")
@Table(name = "teacher_reflections", uniqueConstraints = @UniqueConstraint(name = "teacherReflectionsUniqueKey", columnNames = {
    "runId", "periodId", "nodeId", "componentId",
    "questionId" }), indexes = @Index(columnList = "runId", name = "teacherReflectionsRunIdIndex"))
@Getter
@Setter
public class TeacherReflection {

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

  @Column(name = "questionId", length = 30, nullable = false)
  private String questionId;

  @Column(name = "questionText", length = 65536, columnDefinition = "text", nullable = false)
  private String questionText;

  @Column(name = "answerText", length = 65536, columnDefinition = "text", nullable = false)
  private String answerText;

  @ManyToOne(targetEntity = WorkgroupImpl.class, fetch = FetchType.LAZY)
  @JoinColumn(name = "answeredByWorkgroupId", nullable = false)
  @JsonIgnore
  private Workgroup answeredByWorkgroup;

  @Column(name = "createdAt", nullable = false)
  private Timestamp createdAt;

  @Column(name = "updatedAt", nullable = false)
  private Timestamp updatedAt;
}
