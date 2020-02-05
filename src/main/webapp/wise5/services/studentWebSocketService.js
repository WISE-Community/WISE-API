'use strict';

class StudentWebSocketService {
  constructor(
      $log,
      $rootScope,
      $stomp,
      AnnotationService,
      ConfigService,
      StudentDataService) {
    this.$rootScope = $rootScope;
    this.$stomp = $stomp;
    this.AnnotationService = AnnotationService;
    this.ConfigService = ConfigService;
    this.StudentDataService = StudentDataService;
    this.$stomp.setDebug(function (args) {
      $log.debug(args)
    });
  }

  initialize() {
    this.runId = this.ConfigService.getRunId();
    this.periodId = this.ConfigService.getPeriodId();
    this.workgroupId = this.ConfigService.getWorkgroupId();
    try {
      this.$stomp.connect(this.ConfigService.getWebSocketURL()).then((frame) => {
        this.subscribeToClassroomTopic();
        this.subscribeToWorkgroupTopic();
      });
    } catch(e) {
      console.log(e);
    }
  }

  subscribeToClassroomTopic() {
    this.$stomp.subscribe(`/topic/classroom/${this.runId}/${this.periodId}`, (message, headers, res) => {
      if (message.type === 'pause') {
        this.$rootScope.$broadcast('pauseScreen', {data: message.content});
      } else if (message.type === 'unpause') {
        this.$rootScope.$broadcast('unPauseScreen', {data: message.content});
      } else if (message.type === 'studentWork') {
        const studentWork = JSON.parse(message.content);
        this.$rootScope.$broadcast('studentWorkReceived', studentWork);
      } else if (message.type === 'goToNode') {
        this.goToStep(message);
      }
    });
  }

  subscribeToWorkgroupTopic() {
    this.$stomp.subscribe(`/topic/workgroup/${this.workgroupId}`, (message, headers, res) => {
      if (message.type === 'notification') {
        const notification = JSON.parse(message.content);
        this.$rootScope.$broadcast('newNotificationReceived', notification);
      } else if (message.type === 'annotation') {
        const annotationData = JSON.parse(message.content);
        this.AnnotationService.addOrUpdateAnnotation(annotationData);
        this.$rootScope.$broadcast('newAnnotationReceived', {annotation: annotationData});
      } else if (message.type === 'echoAgent') {
        const echoResponse = JSON.parse(message.content);
        console.log(echoResponse.echoResponse);
        this.$rootScope.$broadcast('echoResponseReceived', echoResponse);
      } else if (message.type === 'goToNode') {
        this.goToStep(message);
      }
    });
  }

  goToStep(message) {
    const content = JSON.parse(message.content);
    this.StudentDataService.endCurrentNodeAndSetCurrentNodeByNodeId(content.data.nodeId);
  }
}

StudentWebSocketService.$inject = [
  '$log',
  '$rootScope',
  '$stomp',
  'AnnotationService',
  'ConfigService',
  'StudentDataService'
];

export default StudentWebSocketService;
