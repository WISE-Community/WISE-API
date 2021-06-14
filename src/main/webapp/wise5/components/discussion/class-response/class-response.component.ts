import {
  Component,
  EventEmitter,
  Input,
  Output,
  SimpleChanges,
  ViewEncapsulation
} from '@angular/core';
import { AnnotationService } from '../../../services/annotationService';
import { ConfigService } from '../../../services/configService';

@Component({
  selector: 'class-response',
  templateUrl: 'class-response.component.html',
  styleUrls: ['class-response.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class ClassResponse {
  @Output()
  createupvoteannotation: any = new EventEmitter();

  @Output()
  createunvoteannotation: any = new EventEmitter();

  @Output()
  createdownvoteannotation: any = new EventEmitter();

  @Input()
  componentannotations: any;

  @Input()
  response: any;

  @Input()
  numreplies: number;

  @Input()
  mode: any;

  @Input()
  isdisabled: any;

  @Input()
  isvotingallowed: boolean;

  @Input()
  iscommentingallowed: boolean = true;

  @Output()
  submitButtonClicked: any = new EventEmitter();

  @Output()
  deleteButtonClicked: any = new EventEmitter();

  @Output()
  undoDeleteButtonClicked: any = new EventEmitter();

  urlMatcher: any = /((http:\/\/www\.|https:\/\/www\.|http:\/\/|https:\/\/)?[a-z0-9]+([\-\.]{1}[a-z0-9]+)*\.[a-z]{2,5}(:[0-9]{1,5})?(\/.*)?)/g;
  expanded: boolean = false;
  repliesToShow: any[] = [];
  currentVote = 0;
  numVotes = 0;
  isUpvoteClicked = false;
  isDownvoteClicked = false;

  constructor(private ConfigService: ConfigService, private AnnotationService: AnnotationService) {}

  ngOnInit(): void {
    this.isdisabled = this.isdisabled === 'true';
    this.injectLinksIntoResponse();
    this.injectLinksIntoReplies();
    if (this.hasAnyReply()) {
      this.showLastReply();
    }
    this.updateVoteDisplays();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.numreplies != null && !changes.numreplies.isFirstChange()) {
      this.expanded = true;
      this.injectLinksIntoReplies();
      this.showAllReplies();
    } else if (changes.componentannotations) {
      this.updateVoteDisplays();
    }
  }

  injectLinksIntoResponse(): void {
    this.response.studentData.responseTextHTML = this.injectLinks(
      this.response.studentData.response
    );
  }

  injectLinksIntoReplies(): void {
    this.response.replies.forEach((replyComponentState: any): void => {
      replyComponentState.studentData.responseHTML = this.injectLinks(
        replyComponentState.studentData.response
      );
    });
  }

  injectLinks(response: string): string {
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
      if (annotation.type === 'vote' && annotation.studentWorkId === this.response.id) {
        this.numVotes += annotation.data.value;
      }
    }
  }

  getLatestVoteForCurrentWorkgroup() {
    for (let i = this.componentannotations.length - 1; i >= 0; i--) {
      const componentannotation = this.componentannotations[i];
      if (
        componentannotation.studentWorkId === this.response.id &&
        componentannotation.fromWorkgroupId === this.ConfigService.getWorkgroupId()
      ) {
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

  getAvatarColorForWorkgroupId(workgroupId: number): string {
    return this.ConfigService.getAvatarColorForWorkgroupId(workgroupId);
  }

  replyEntered($event: any): void {
    if (this.isEnterKeyEvent($event)) {
      $event.preventDefault();
      this.response.replyText = this.removeLastChar(this.response.replyText);
      this.expandAndShowAllReplies();
      this.submitButtonClicked.emit(this.response);
    }
  }

  isEnterKeyEvent(event: any): boolean {
    return event.keyCode == 13 && !event.shiftKey && this.response.replyText;
  }

  removeLastChar(responseText: string): string {
    return responseText.substring(0, responseText.length - 1);
  }

  delete(componentState: any): void {
    if (confirm($localize`Are you sure you want to delete this post?`)) {
      this.deleteButtonClicked.emit(componentState);
    }
  }

  adjustClientSaveTime(time: any): number {
    return this.ConfigService.convertToClientTimestamp(time);
  }

  undoDelete(componentState: any): void {
    if (confirm($localize`Are you sure you want to show this post?`)) {
      this.undoDeleteButtonClicked.emit(componentState);
    }
  }

  toggleExpanded(): void {
    this.expanded = !this.expanded;
    if (this.expanded) {
      this.showAllReplies();
    } else {
      this.showLastReply();
    }
  }

  hasAnyReply(): boolean {
    return this.response.replies.length > 0;
  }

  showLastReply(): void {
    if (this.response.replies.length > 0) {
      this.repliesToShow = [this.response.replies[this.response.replies.length - 1]];
    }
  }

  showAllReplies(): void {
    this.repliesToShow = this.response.replies;
  }

  expandAndShowAllReplies(): void {
    this.expanded = true;
    this.showAllReplies();
  }

  upvoteClicked(componentState) {
    if (!this.isUpvoteClicked) {
      this.createupvoteannotation.emit({ componentState: componentState });
    } else {
      this.createunvoteannotation.emit({ componentState: componentState });
    }
  }

  downvoteClicked(componentState) {
    if (!this.isDownvoteClicked) {
      this.createdownvoteannotation.emit({ componentState: componentState });
    } else {
      this.createunvoteannotation.emit({ componentState: componentState });
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
      const latestInappropriateFlagAnnotation = this.AnnotationService.getLatestAnnotationByStudentWorkIdAndType(
        componentState.id,
        'vote'
      );
      if (latestInappropriateFlagAnnotation != null) {
        annotations.push(latestInappropriateFlagAnnotation);
      }
    }
    return annotations;
  }
}
