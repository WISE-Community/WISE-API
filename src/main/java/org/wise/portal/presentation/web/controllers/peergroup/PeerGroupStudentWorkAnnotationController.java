package org.wise.portal.presentation.web.controllers.peergroup;

import java.io.IOException;
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
import org.wise.vle.domain.work.StudentWorkAnnotation;

import lombok.Getter;

@RestController
@Secured("ROLE_USER")
@RequestMapping("/api/peer-group/{peerGroupId}/{nodeId}/{componentId}/student-work-annotation")
public class PeerGroupStudentWorkAnnotationController extends AbstractPeerGroupWorkController {

  @Autowired
  private AnnotationService annotationService;

  @GetMapping("/dynamic-prompt/reference-component/latest-auto-score")
  List<StudentWorkAnnotation> getListWithLatestAutoScoreForDynamicPromptReferenceComponent(
      @PathVariable("peerGroupId") PeerGroupImpl peerGroup, @PathVariable String nodeId,
      @PathVariable String componentId, Authentication auth) throws Exception {
    if (isUserInPeerGroup(auth, peerGroup)) {
      DynamicPrompt dynamicPrompt = getDynamicPrompt(peerGroup.getPeerGrouping().getRun(), nodeId,
          componentId);
      List<Annotation> annotations = annotationService.getLatest(peerGroup.getMembers(),
          dynamicPrompt.getReferenceNodeId(), dynamicPrompt.getReferenceComponentId(), "autoScore");
      return annotations.stream().map(annotation -> new StudentWorkAnnotation(annotation))
          .collect(Collectors.toList());
    } else {
      throw new AccessDeniedException("Not permitted");
    }
  }

  private DynamicPrompt getDynamicPrompt(Run run, String nodeId, String componentId)
      throws IOException, JSONException, ObjectNotFoundException {
    ProjectComponent projectComponent = getProjectComponent(run, nodeId, componentId);
    JSONObject dynamicPromptJSON = projectComponent.getJSONObject("dynamicPrompt");
    return new DynamicPrompt(dynamicPromptJSON);
  }
}

@Getter
class DynamicPrompt {
  String peerGroupingTag;
  String referenceComponentId;
  String referenceNodeId;

  public DynamicPrompt(JSONObject content) throws JSONException {
    this.peerGroupingTag = content.getString("peerGroupingTag");
    JSONObject referenceComponentJSON = content.getJSONObject("referenceComponent");
    this.referenceNodeId = referenceComponentJSON.getString("nodeId");
    this.referenceComponentId = referenceComponentJSON.getString("componentId");
  }
}
