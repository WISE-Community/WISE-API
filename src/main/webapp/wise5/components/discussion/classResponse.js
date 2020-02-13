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
  }

  $onInit() {
    this.injectLinksIntoResponse();
    
    //TODO: Sum the votes
    /*const annotations = this.AnnotationService.getAnnotationsByStudentWorkId(this.response.id)
    let currentVote;

    for (let annotation of annotations) {
      if (annotation.type == "vote"){
        this.numVotes += annotation.data.value; 
      }
    }*/
    
    //TODO: Get the current vote for the current workgroup 
    /*currentVote = this.AnnotationService.getLatestVoteAnnotationByStudentWorkIdAndFromWorkgroupId(this.response.id, userInfo.workgroupId);
    if (currentVote != null) {
      this.currentVote = currentVote;
    }
    else {
      this.currentVote = 0;
    }
    
    */
    
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

  updateNumVotes(newVote) {
    this.numVotes += (newVote - this.currentVote);
    this.currentVote = newVote;
  }

  upvoteClicked(componentState) {
    var isUpvoteClicked = !this.isUpvoteClicked;
    var newVote;
    
    if (isUpvoteClicked) {
      newVote = 1;
      this.createupvoteannotation({componentState: componentState});
      this.isUpvoteClicked = true;
      this.isDownvoteClicked = false;
      this.updateNumVotes(newVote);
    }
    else {
      newVote = 0;
      this.createunvoteannotation({componentState: componentState});
      this.isUpvoteClicked = false;
      this.isDownvoteClicked = false;
      this.updateNumVotes(newVote);
    }
  }

  downvoteClicked(componentState) {
    var isDownvoteClicked = !this.isDownvoteClicked;
    var newVote;
    
    if (isDownvoteClicked) {
      newVote = -1;
      this.createdownvoteannotation({componentState: componentState});
      this.isUpvoteClicked = false;
      this.isDownvoteClicked = true;
      this.updateNumVotes(newVote);
    }
    else {
      newVote = 0;
      this.createUnvoteAnnotation({componentState: componentState});
      this.isUpvoteClicked = false;
      this.isUpvoteClicked = false; 
      this.updateNumVotes(newVote);
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
    mode: '@',
    deletebuttonclicked: '&',
    undodeletebuttonclicked: '&',
    createupvoteannotation: '&',
    createdownvoteannotation: '&',
    createunvoteannotation: '&',
    submitbuttonclicked: '&',
    studentdatachanged: '&',
    isdisabled: '<'
  },
  templateUrl: 'wise5/components/discussion/classResponse.html',
  controller: 'ClassResponseController as classResponseCtrl'
};

export { ClassResponseController, ClassResponseComponentOptions };
