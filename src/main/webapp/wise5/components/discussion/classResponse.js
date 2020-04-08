'use strict';

class ClassResponseController {
  constructor($scope, $filter, AnnotationService, StudentStatusService, ConfigService) {
    this.$scope = $scope;
    this.$filter = $filter;
    this.AnnotationService = AnnotationService;
    this.StudentStatusService = StudentStatusService;
    this.ConfigService = ConfigService;
    this.$translate = this.$filter('translate');
    this.urlMatcher = /((http:\/\/www\.|https:\/\/www\.|http:\/\/|https:\/\/)?[a-z0-9]+([\-\.]{1}[a-z0-9]+)*\.[a-z]{2,5}(:[0-9]{1,5})?(\/.*)?)/g;
    this.expanded = false;
    this.currentVote = 0;
    this.numVotes = 0;
    this.isUpvoteClicked = false;
    this.isDownvoteClicked = false;

    this.$scope.$watch(
      () => { return this.response.replies.length; },
      (numNew, numOld) => {
        if (numNew !== numOld) {
          this.expanded = true;
        }
      }
    );

    this.$scope.$watch(
      () => { return this.componentannotations; },
      (numNew, numOld) => {
        this.updateVoteDisplays();
      }
    );
  }

  $onInit() {
    this.injectLinksIntoResponse();
    this.updateVoteDisplays();
  }

  injectLinksIntoResponse() {
    this.response.studentData.responseText = this.injectLinks(this.response.studentData.response);
  }

  injectLinks(response) {
    return response.replace(this.urlMatcher, (match) => {
      let matchUrl = match;
      if (!match.startsWith('http')) {
        /*
         * The url does not begin with http so we will add // to the beginning of it so that the
         * browser treats the url as an absolute link and not a relative link. The browser will also
         * use the same protocol that the current page is loaded with (http or https).
         */
        matchUrl = '//' + match;
      }
      return `<a href="${matchUrl}" target="_blank">${match}</a>`;
    });
  }

  updateVoteDisplays() {
    this.sumVotes();
    this.getLatestVoteForCurrentWorkgroup();
  }

  sumVotes() {
    this.numVotes = 0;
    for (const annotation of this.componentannotations) {
      if (annotation.type === "vote" && annotation.studentWorkId === this.response.id) {
        this.numVotes += annotation.data.value;
      }
    }
  }

  getLatestVoteForCurrentWorkgroup() {
    for (let i = this.componentannotations.length - 1; i >= 0; i--) {
      const componentannotation = this.componentannotations[i];
      if (componentannotation.studentWorkId === this.response.id && componentannotation.fromWorkgroupId === this.ConfigService.getWorkgroupId()) {
        if (componentannotation.data.value === -1) {
          this.isDownvoteClicked = true;
          this.isUpvoteClicked = false;
        } else if (componentannotation.data.value === 1) {
          this.isDownvoteClicked = false;
          this.isUpvoteClicked = true;
        } else {
          this.isDownvoteClicked = false;
          this.isUpvoteClicked = false;
        }
        break;
      }
    }
  }

  getAvatarColorForWorkgroupId(workgroupId) {
    return this.ConfigService.getAvatarColorForWorkgroupId(workgroupId);
  }

  replyEntered($event) {
    if($event.keyCode == 13 && !$event.shiftKey && this.response.replyText) {
      $event.preventDefault();
      this.submitbuttonclicked({r: this.response});
    }
  }

  deleteButtonClicked(componentState) {
    if (confirm(this.$translate('discussion.areYouSureYouWantToDeleteThisPost'))) {
      this.deletebuttonclicked({componentState: componentState});
    }
  }

  undoDeleteButtonClicked(componentState) {
    if (confirm(this.$translate('discussion.areYouSureYouWantToShowThisPost'))) {
      this.undodeletebuttonclicked({componentState: componentState});
    }
  }

  toggleExpanded() {
    this.expanded = !this.expanded;
  }

  adjustClientSaveTime(time) {
    return this.ConfigService.convertToClientTimestamp(time);
  }

  upvoteClicked(componentState) {
    if (!this.isUpvoteClicked) {
      this.createupvoteannotation({componentState: componentState});
    } else {
      this.createunvoteannotation({componentState: componentState});
    }
  }

  downvoteClicked(componentState) {
    if (!this.isDownvoteClicked) {
      this.createdownvoteannotation({componentState: componentState});
    } else {
      this.createunvoteannotation({componentState: componentState});
    }
  }

  /**
   * Get the vote annotations for these component states
   * @param componentStates an array of component states
   * @return an array of vote annotations that are associated
   * with the component states
   */
  getVoteAnnotationsByComponentStates(componentStates = []) {
    const annotations = [];
    for (const componentState of componentStates) {
      const latestInappropriateFlagAnnotation =
          this.AnnotationService.getLatestAnnotationByStudentWorkIdAndType(
          componentState.id, 'vote');
      if (latestInappropriateFlagAnnotation != null) {
        annotations.push(latestInappropriateFlagAnnotation);
      }
    }
    return annotations;
  }
}

ClassResponseController.$inject = ['$scope','$filter','AnnotationService','StudentStatusService','ConfigService'];

const ClassResponseComponentOptions = {
  bindings: {
    response: '<',
    componentannotations: '<',
    mode: '@',
    deletebuttonclicked: '&',
    undodeletebuttonclicked: '&',
    createupvoteannotation: '&',
    createdownvoteannotation: '&',
    createunvoteannotation: '&',
    submitbuttonclicked: '&',
    studentdatachanged: '&',
    isdisabled: '<',
    isvotingallowed: '<'
  },
  templateUrl: 'wise5/components/discussion/classResponse.html',
  controller: 'ClassResponseController as classResponseCtrl'
};

export { ClassResponseController, ClassResponseComponentOptions };
