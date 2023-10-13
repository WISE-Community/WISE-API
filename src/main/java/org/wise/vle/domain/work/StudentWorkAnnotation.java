package org.wise.vle.domain.work;

import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.vle.domain.annotation.wise5.Annotation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentWorkAnnotation {

  private Annotation annotation;
  private StudentWork studentWork;
  private Workgroup workgroup;

  public StudentWorkAnnotation(Annotation annotation) {
    this.annotation = annotation;
    this.studentWork = annotation.getStudentWork();
    this.workgroup = annotation.getToWorkgroup();
  }

  public StudentWorkAnnotation(StudentWork studentWork) {
    this.studentWork = studentWork;
    this.workgroup = studentWork.getWorkgroup();
  }
}
