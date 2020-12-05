'use strict';

import { Directive } from '@angular/core';
import { EditComponentController } from '../../authoringTool/components/editComponentController';

@Directive()
class DiscussionAuthoringController extends EditComponentController {

  allowedConnectedComponentTypes: any[] = [{ type: 'Discussion' }];

  static $inject = [
    '$filter',
    'ConfigService',
    'NodeService',
    'NotificationService',
    'ProjectAssetService',
    'ProjectService',
    'UtilService',
  ];

  constructor(
    $filter,
    ConfigService,
    NodeService,
    NotificationService,
    ProjectAssetService,
    ProjectService,
    UtilService
  ) {
    super(
      $filter,
      ConfigService,
      NodeService,
      NotificationService,
      ProjectAssetService,
      ProjectService,
      UtilService,
    );
  }

  connectedComponentTypeChanged(connectedComponent) {
    this.changeAllDiscussionConnectedComponentTypesToMatch(connectedComponent.type);
    super.connectedComponentTypeChanged(connectedComponent);
  }

  changeAllDiscussionConnectedComponentTypesToMatch(connectedComponentType) {
    for (const connectedComponent of this.authoringComponentContent.connectedComponents) {
      connectedComponent.type = connectedComponentType;
    }
  }

  automaticallySetConnectedComponentTypeIfPossible(connectedComponent) {
    if (connectedComponent.componentId != null) {
      const firstConnectedComponent = this.authoringComponentContent.connectedComponents[0];
      connectedComponent.type = firstConnectedComponent.type;
    }
  }
}

const DiscussionAuthoring = {
  bindings: {
    nodeId: '@',
    componentId: '@'
  },
  controller: DiscussionAuthoringController,
  controllerAs: 'discussionController',
  templateUrl: 'wise5/components/discussion/authoring.html'
}

export default DiscussionAuthoring;
