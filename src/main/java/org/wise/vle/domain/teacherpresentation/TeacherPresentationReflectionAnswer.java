package org.wise.vle.domain.teacherpresentation;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.domain.workgroup.impl.WorkgroupImpl;

/**
 * A teacher's answer to one reflection question of a TeacherPresentation. Only the latest answer
 * per question is kept. The question text is copied at answer time so research data survives later
 * edits to the question.
 */
@Entity(name = "teacherPresentationReflectionAnswer")
@Table(name = "teacher_presentation_reflection_answers",
    uniqueConstraints = @UniqueConstraint(
        name = "teacherPresentationReflectionAnswersUniqueKey",
        columnNames = { "teacherPresentationConfigId", "questionId" }))
@Getter
@Setter
public class TeacherPresentationReflectionAnswer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "teacherPresentationConfigId", nullable = false)
  @JsonIgnore
  private TeacherPresentationConfig teacherPresentationConfig;

  @Column(name = "questionId", length = 30, nullable = false)
  private String questionId;

  @Column(name = "questionText", length = 65536, columnDefinition = "text", nullable = false)
  private String questionText;

  @Column(name = "answerText", length = 65536, columnDefinition = "text")
  private String answerText;

  @ManyToOne(targetEntity = WorkgroupImpl.class, fetch = FetchType.LAZY)
  @JoinColumn(name = "answeredByWorkgroupId")
  @JsonIgnore
  private Workgroup answeredByWorkgroup;

  @Column(name = "createdAt", nullable = false)
  private Timestamp createdAt;

  @Column(name = "updatedAt", nullable = false)
  private Timestamp updatedAt;
}
