package org.wise.portal.presentation.web.controllers.peergroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.peergroup.impl.PeerGroupImpl;
import org.wise.portal.domain.project.impl.ProjectComponent;
import org.wise.portal.domain.run.Run;
import org.wise.portal.presentation.web.controllers.student.AbstractPeerGroupWorkController;
import org.wise.portal.service.vle.wise5.AnnotationService;
import org.wise.vle.domain.annotation.wise5.Annotation;
import org.wise.vle.domain.work.StudentWork;
import org.wise.vle.domain.work.StudentWorkAnnotation;

import lombok.Getter;

@RestController
@Secured("ROLE_USER")
@RequestMapping("/api/peer-group/{peerGroupId}/{nodeId}/{componentId}/student-data")
public class PeerGroupStudentWorkAnnotationController extends AbstractPeerGroupWorkController {

  @Autowired
  private AnnotationService annotationService;

  @GetMapping("/dynamic-prompt")
  List<StudentWorkAnnotation> getStudentDataForDynamicPrompt(
      @PathVariable("peerGroupId") PeerGroupImpl peerGroup, @PathVariable String nodeId,
      @PathVariable String componentId, Authentication auth) throws Exception {
    checkPermissions(auth, peerGroup);
    return getStudentDataForReferenceComponent(peerGroup, nodeId, componentId, "dynamicPrompt");
  }

  @GetMapping("/question-bank")
  List<StudentWorkAnnotation> getStudentDataForQuestionBank(
      @PathVariable("peerGroupId") PeerGroupImpl peerGroup, @PathVariable String nodeId,
      @PathVariable String componentId, Authentication auth) throws Exception {
    checkPermissions(auth, peerGroup);
    return getStudentDataForReferenceComponent(peerGroup, nodeId, componentId, "questionBank");
  }

  private void checkPermissions(Authentication auth, PeerGroupImpl peerGroup)
      throws ObjectNotFoundException {
    if (!isUserInPeerGroup(auth, peerGroup)) {
      throw new AccessDeniedException("Not permitted");
    }
  }

  private List<StudentWorkAnnotation> getStudentDataForReferenceComponent(PeerGroupImpl peerGroup,
      String nodeId, String componentId, String fieldName) throws Exception {
    Run run = peerGroup.getPeerGrouping().getRun();
    ReferenceComponent component = getReferenceComponent(run, nodeId, componentId, fieldName);
    String referenceComponentType = getProjectComponent(run, component.nodeId,
        component.componentId).getType();
    if (referenceComponentType.equals("MultipleChoice")) {
      return getStudentDataForMultipleChoice(peerGroup, component);
    } else if (referenceComponentType.equals("OpenResponse")) {
      return getStudentDataForOpenResponse(peerGroup, component);
    } else {
      return new ArrayList<StudentWorkAnnotation>();
    }
  }

  private List<StudentWorkAnnotation> getStudentDataForMultipleChoice(PeerGroupImpl peerGroup,
      ReferenceComponent component) {
    List<StudentWork> studentWorkList = studentWorkService.getStudentWork(peerGroup.getMembers(),
        component.getNodeId(), component.getComponentId());
    return studentWorkList.stream().map(studentWork -> new StudentWorkAnnotation(studentWork))
        .collect(Collectors.toList());
  }

  private List<StudentWorkAnnotation> getStudentDataForOpenResponse(PeerGroupImpl peerGroup,
      ReferenceComponent component) {
    List<Annotation> annotations = annotationService.getLatest(peerGroup.getMembers(),
        component.getNodeId(), component.getComponentId(), "autoScore");
    return annotations.stream().map(annotation -> new StudentWorkAnnotation(annotation))
        .collect(Collectors.toList());
  }

  private ReferenceComponent getReferenceComponent(Run run, String nodeId, String componentId,
      String fieldName) throws IOException, JSONException, ObjectNotFoundException {
    ProjectComponent projectComponent = getProjectComponent(run, nodeId, componentId);
    JSONObject referenceComponentJSON = projectComponent.getJSONObject(fieldName);
    return new ReferenceComponent(referenceComponentJSON);
  }
}

@Getter
class ReferenceComponent {
  String peerGroupingTag;
  String componentId;
  String nodeId;

  public ReferenceComponent(JSONObject content) throws JSONException {
    this.peerGroupingTag = content.getString("peerGroupingTag");
    JSONObject referenceComponentJSON = content.getJSONObject("referenceComponent");
    this.nodeId = referenceComponentJSON.getString("nodeId");
    this.componentId = referenceComponentJSON.getString("componentId");
  }
}
