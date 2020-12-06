'use strict';

import * as angular from 'angular';
import * as moment from 'moment';
import { AchievementService } from './achievementService';
import { AnnotationService } from './annotationService';
import { ConfigService } from './configService';
import { ProjectService } from './projectService';
import { TeacherDataService } from './teacherDataService';
import { UtilService } from './utilService';
import { Injectable } from '@angular/core';
import { UpgradeModule } from '@angular/upgrade/static';

@Injectable()
export class MilestoneService {
  numberOfStudentsCompletedStorage: any[] = [];
  numberOfStudentsInRun: number;
  percentageCompletedStorage: any[] = [];
  periodId: any;
  projectMilestones: any[];
  workgroupIds: any[];
  workgroupsStorage: any[] = [];

  constructor(
    private upgrade: UpgradeModule,
    private AchievementService: AchievementService,
    private AnnotationService: AnnotationService,
    private ConfigService: ConfigService,
    private ProjectService: ProjectService,
    private TeacherDataService: TeacherDataService,
    private UtilService: UtilService
  ) {
  }

  getTranslation(key: string, args: any = null) {
    if (args == null) {
      return this.upgrade.$injector.get('$filter')('translate')(key);
    } else {
      return this.upgrade.$injector.get('$filter')('translate')(key, args);
    }
  }

  getProjectMilestones() {
    let milestones = [];
    const projectAchievements = this.ProjectService.getAchievements();
    if (projectAchievements.isEnabled) {
      milestones = projectAchievements.items.filter(achievement => {
        return achievement.type === 'milestone' || achievement.type === 'milestoneReport';
      });
    }
    return milestones;
  }

  getProjectMilestoneReports() {
    return this.getProjectMilestones().filter(milestone => {
      return milestone.type === 'milestoneReport';
    });
  }

  getMilestoneReportByNodeId(nodeId: string) {
    const milestoneReports = this.getProjectMilestoneReports();
    for (const milestonReport of milestoneReports) {
      const referencedComponent = this.getReferencedComponent(milestonReport);
      if (referencedComponent.nodeId === nodeId) {
        return this.getProjectMilestoneStatus(milestonReport.id);
      }
    }
    return null;
  }

  getProjectMilestoneStatus(milestoneId: string) {
    this.periodId = this.TeacherDataService.getCurrentPeriod().periodId;
    this.setWorkgroupsInCurrentPeriod();
    let milestone = this.ProjectService.getAchievementByAchievementId(milestoneId);
    milestone = this.insertMilestoneItems(milestone);
    milestone = this.insertMilestoneCompletion(milestone);
    if (milestone.type === 'milestoneReport') {
      milestone = this.insertMilestoneReport(milestone);
    }
    return milestone;
  }

  insertMilestoneItems(milestone: any) {
    milestone.items = this.UtilService.makeCopyOfJSONObject(this.ProjectService.idToOrder);
    if (milestone.params != null && milestone.params.nodeIds != null) {
      for (const nodeId of milestone.params.nodeIds) {
        if (milestone.items[nodeId] != null) {
          milestone.items[nodeId].checked = true;
        }
      }
    }
    return milestone;
  }

  insertMilestoneCompletion(milestone: any) {
    const achievementIdToStudentAchievements =
        this.AchievementService.getAchievementIdToStudentAchievementsMappings();
    const studentAchievements = achievementIdToStudentAchievements[milestone.id];
    const workgroupIdsCompleted = [];
    const achievementTimes = [];
    const workgroupIdsNotCompleted = [];

    for (const studentAchievement of studentAchievements) {
      const currentWorkgroupId = studentAchievement.workgroupId;
      if (this.workgroupIds.indexOf(currentWorkgroupId) > -1) {
        workgroupIdsCompleted.push(currentWorkgroupId);
        achievementTimes.push(studentAchievement.achievementTime);
      }
    }

    for (const workgroupId of this.workgroupIds) {
      if (workgroupIdsCompleted.indexOf(workgroupId) === -1) {
        workgroupIdsNotCompleted.push(workgroupId);
      }
    }

    milestone.workgroups = [];

    for (let c = 0; c < workgroupIdsCompleted.length; c++) {
      const workgroupId = workgroupIdsCompleted[c];
      const achievementTime = achievementTimes[c];
      const workgroupObject = {
        workgroupId: workgroupId,
        displayNames: this.getDisplayUsernamesByWorkgroupId(workgroupId),
        achievementTime: achievementTime,
        completed: true
      };
      milestone.workgroups.push(workgroupObject);
    }

    for (const workgroupId of workgroupIdsNotCompleted) {
      const workgroupObject = {
        workgroupId: workgroupId,
        displayNames: this.getDisplayUsernamesByWorkgroupId(workgroupId),
        achievementTime: null,
        completed: false
      };
      milestone.workgroups.push(workgroupObject);
    }

    milestone.numberOfStudentsCompleted = workgroupIdsCompleted.length;
    milestone.numberOfStudentsInRun = this.numberOfStudentsInRun;
    milestone.percentageCompleted = Math.round(
      (100 * milestone.numberOfStudentsCompleted) / this.numberOfStudentsInRun
    );
    return milestone;
  }

  setWorkgroupsInCurrentPeriod() {
    const workgroupIdsInRun = this.ConfigService.getClassmateWorkgroupIds();
    this.workgroupIds = [];
    for (let i = 0; i < workgroupIdsInRun.length; i++) {
      const currentId = workgroupIdsInRun[i];
      const currentPeriodId = this.ConfigService.getPeriodIdByWorkgroupId(currentId);

      if (this.periodId === -1 || currentPeriodId === this.periodId) {
        this.workgroupIds.push(currentId);
      }
    }
    this.numberOfStudentsInRun = this.workgroupIds.length;
  }

  insertMilestoneReport(milestone: any) {
    const referencedComponent = this.getReferencedComponent(milestone);
    milestone.nodeId = referencedComponent.nodeId;
    milestone.componentId = referencedComponent.componentId;
    if (this.isCompletionReached(milestone)) {
      const report = this.generateReport(milestone);
      this.setReportAvailable(milestone, true);
      milestone.generatedReport = report.content ? report.content : null;
      milestone.generatedRecommendations = report.recommendations ? report.recommendations : null;
    } else {
      this.setReportAvailable(milestone, false);
    }
    return milestone;
  }

  getReferencedComponent(milestone: any) {
    const referencedComponents = this.getSatisfyCriteriaReferencedComponents(milestone);
    const referencedComponentValues: any[] = Object.values(referencedComponents);
    return referencedComponentValues[referencedComponentValues.length - 1];
  }

  getDisplayUsernamesByWorkgroupId(workgroupId: number) {
    return this.ConfigService.getDisplayUsernamesByWorkgroupId(workgroupId);
  }

  isCompletionReached(projectAchievement: any) {
    return (
      projectAchievement.percentageCompleted >= projectAchievement.satisfyMinPercentage &&
      projectAchievement.numberOfStudentsCompleted >= projectAchievement.satisfyMinNumWorkgroups
    );
  }

  generateReport(projectAchievement: any) {
    const referencedComponents = this.getSatisfyCriteriaReferencedComponents(projectAchievement);
    const aggregateAutoScores = {};
    let nodeId = null;
    let componentId = null;
    const referencedComponentValues: any[] = Object.values(referencedComponents);
    for (const referencedComponent of referencedComponentValues) {
      nodeId = referencedComponent.nodeId;
      componentId = referencedComponent.componentId;
      aggregateAutoScores[componentId] = this.calculateAggregateAutoScores(
        nodeId,
        componentId,
        this.periodId,
        projectAchievement.report
      );
    }
    const template = this.chooseTemplate(projectAchievement.report.templates, aggregateAutoScores);
    let content = template.content ? template.content : '';
    if (content) {
      content = this.processMilestoneGraphsAndData(content, aggregateAutoScores);
    }
    const recommendations = template.recommendations ? template.recommendations : '';
    return {
      content: content,
      recommendations: recommendations,
      nodeId: nodeId,
      componentId: componentId
    };
  }

  chooseTemplate(templates: any[], aggregateAutoScores: any) {
    for (const template of templates) {
      if (this.isTemplateMatch(template, aggregateAutoScores)) {
        return template;
      }
    }
    return {
      content: null
    };
  }

  isTemplateMatch(template: any, aggregateAutoScores: any) {
    const matchedCriteria = [];
    for (const satisfyCriterion of template.satisfyCriteria) {
      if (this.isTemplateCriterionSatisfied(satisfyCriterion, aggregateAutoScores)) {
        matchedCriteria.push(satisfyCriterion);
      }
    }
    if (template.satisfyConditional === 'all') {
      return matchedCriteria.length === template.satisfyCriteria.length;
    } else if (template.satisfyConditional === 'any') {
      return matchedCriteria.length > 0;
    }
  }

  isTemplateCriterionSatisfied(satisfyCriterion: any, aggregateAutoScores: any) {
    if (satisfyCriterion.function === 'percentOfScoresGreaterThan') {
      return this.isPercentOfScoresGreaterThan(satisfyCriterion, aggregateAutoScores);
    } else if (satisfyCriterion.function === 'percentOfScoresGreaterThanOrEqualTo') {
      return this.isPercentOfScoresGreaterThanOrEqualTo(satisfyCriterion, aggregateAutoScores);
    } else if (satisfyCriterion.function === 'percentOfScoresLessThan') {
      return this.isPercentOfScoresLessThan(satisfyCriterion, aggregateAutoScores);
    } else if (satisfyCriterion.function === 'percentOfScoresLessThanOrEqualTo') {
      return this.isPercentOfScoresLessThanOrEqualTo(satisfyCriterion, aggregateAutoScores);
    } else if (satisfyCriterion.function === 'percentOfScoresEqualTo') {
      return this.isPercentOfScoresEqualTo(satisfyCriterion, aggregateAutoScores);
    } else if (satisfyCriterion.function === 'percentOfScoresNotEqualTo') {
      return this.isPercentOfScoresNotEqualTo(satisfyCriterion, aggregateAutoScores);
    } else if (satisfyCriterion.function === 'default') {
      return true;
    }
  }

  isPercentOfScoresGreaterThan(satisfyCriterion: any, aggregateAutoScores: any) {
    const aggregateData = this.getAggregateData(satisfyCriterion, aggregateAutoScores);
    const possibleScores = this.getPossibleScores(aggregateData);
    const sum = this.getGreaterThanSum(satisfyCriterion, aggregateData, possibleScores);
    return this.isPercentThresholdSatisfied(satisfyCriterion, aggregateData, sum);
  }

  getGreaterThanSum(satisfyCriterion: any, aggregateData: any, possibleScores: number[]) {
    let sum = 0;
    for (const possibleScore of possibleScores) {
      if (possibleScore > satisfyCriterion.value) {
        sum += aggregateData.counts[possibleScore];
      }
    }
    return sum;
  }

  isPercentOfScoresGreaterThanOrEqualTo(satisfyCriterion: any, aggregateAutoScores: any) {
    const aggregateData = this.getAggregateData(satisfyCriterion, aggregateAutoScores);
    const possibleScores = this.getPossibleScores(aggregateData);
    const sum = this.getGreaterThanOrEqualToSum(satisfyCriterion, aggregateData, possibleScores);
    return this.isPercentThresholdSatisfied(satisfyCriterion, aggregateData, sum);
  }

  getGreaterThanOrEqualToSum(satisfyCriterion: any, aggregateData: any, possibleScores: number[]) {
    let sum = 0;
    for (const possibleScore of possibleScores) {
      if (possibleScore >= satisfyCriterion.value) {
        sum += aggregateData.counts[possibleScore];
      }
    }
    return sum;
  }

  isPercentOfScoresLessThan(satisfyCriterion: any, aggregateAutoScores: any) {
    const aggregateData = this.getAggregateData(satisfyCriterion, aggregateAutoScores);
    const possibleScores = this.getPossibleScores(aggregateData);
    const sum = this.getLessThanSum(satisfyCriterion, aggregateData, possibleScores);
    return this.isPercentThresholdSatisfied(satisfyCriterion, aggregateData, sum);
  }

  getLessThanSum(satisfyCriterion: any, aggregateData: any, possibleScores: number[]) {
    let sum = 0;
    for (const possibleScore of possibleScores) {
      if (possibleScore < satisfyCriterion.value) {
        sum += aggregateData.counts[possibleScore];
      }
    }
    return sum;
  }

  isPercentOfScoresLessThanOrEqualTo(satisfyCriterion: any, aggregateAutoScores: any) {
    const aggregateData = this.getAggregateData(satisfyCriterion, aggregateAutoScores);
    const possibleScores = this.getPossibleScores(aggregateData);
    const sum = this.getLessThanOrEqualToSum(satisfyCriterion, aggregateData, possibleScores);
    return this.isPercentThresholdSatisfied(satisfyCriterion, aggregateData, sum);
  }

  getLessThanOrEqualToSum(satisfyCriterion: any, aggregateData: any, possibleScores: number[]) {
    let sum = 0;
    for (const possibleScore of possibleScores) {
      if (possibleScore <= satisfyCriterion.value) {
        sum += aggregateData.counts[possibleScore];
      }
    }
    return sum;
  }

  isPercentOfScoresEqualTo(satisfyCriterion: any, aggregateAutoScores: any) {
    const aggregateData = this.getAggregateData(satisfyCriterion, aggregateAutoScores);
    const possibleScores = this.getPossibleScores(aggregateData);
    const sum = this.getEqualToSum(satisfyCriterion, aggregateData, possibleScores);
    return this.isPercentThresholdSatisfied(satisfyCriterion, aggregateData, sum);
  }

  getEqualToSum(satisfyCriterion: any, aggregateData: any, possibleScores: number[]) {
    let sum = 0;
    for (const possibleScore of possibleScores) {
      if (possibleScore === satisfyCriterion.value) {
        sum += aggregateData.counts[possibleScore];
      }
    }
    return sum;
  }

  isPercentOfScoresNotEqualTo(satisfyCriterion: any, aggregateAutoScores: any) {
    const aggregateData = this.getAggregateData(satisfyCriterion, aggregateAutoScores);
    const possibleScores = this.getPossibleScores(aggregateData);
    const sum = this.getNotEqualToSum(satisfyCriterion, aggregateData, possibleScores);
    return this.isPercentThresholdSatisfied(satisfyCriterion, aggregateData, sum);
  }

  getNotEqualToSum(satisfyCriterion: any, aggregateData: any, possibleScores: number[]) {
    let sum = 0;
    for (const possibleScore of possibleScores) {
      if (possibleScore !== satisfyCriterion.value) {
        sum += aggregateData.counts[possibleScore];
      }
    }
    return sum;
  }

  getAggregateData(satisfyCriterion: any, aggregateAutoScores: any) {
    const component = aggregateAutoScores[satisfyCriterion.componentId];
    return component[satisfyCriterion.targetVariable];
  }

  getPossibleScores(aggregateData: any) {
    return Object.keys(aggregateData.counts)
      .map(Number)
      .sort();
  }

  isPercentThresholdSatisfied(satisfyCriterion: any, aggregateData: any, sum: number) {
    const percentOfScores = (100 * sum) / aggregateData.scoreCount;
    return percentOfScores >= satisfyCriterion.percentThreshold;
  }

  getSatisfyCriteriaReferencedComponents(projectAchievement: any) {
    const components = {};
    const templates = projectAchievement.report.templates;
    for (const template of templates) {
      for (const satisfyCriterion of template.satisfyCriteria) {
        const nodeId = satisfyCriterion.nodeId;
        const componentId = satisfyCriterion.componentId;
        const component = {
          nodeId: nodeId,
          componentId: componentId
        };
        components[nodeId + '_' + componentId] = component;
      }
    }
    return components;
  }

  calculateAggregateAutoScores(nodeId: string, componentId: string, periodId: number,
      reportSettings: any) {
    const aggregate = {};
    const scoreAnnotations = this.AnnotationService.getAllLatestScoreAnnotations(
      nodeId,
      componentId,
      periodId
    );
    for (const scoreAnnotation of scoreAnnotations) {
      if (scoreAnnotation.type === 'autoScore') {
        this.addDataToAggregate(aggregate, scoreAnnotation, reportSettings);
      } else {
        const autoScoreAnnotation = this.AnnotationService.getLatestScoreAnnotation(
          nodeId,
          componentId,
          scoreAnnotation.toWorkgroupId,
          'autoScore'
        );
        if (autoScoreAnnotation) {
          const mergedAnnotation = this.mergeAutoScoreAndTeacherScore(
            autoScoreAnnotation,
            scoreAnnotation,
            reportSettings
          );
          this.addDataToAggregate(aggregate, mergedAnnotation, reportSettings);
        }
      }
    }
    return aggregate;
  }

  mergeAutoScoreAndTeacherScore(autoScoreAnnotation: any, teacherScoreAnnotation: any,
      reportSettings: any) {
    if (autoScoreAnnotation.data.scores) {
      for (const subScore of autoScoreAnnotation.data.scores) {
        if (subScore.id === 'ki') {
          subScore.score = this.adjustKIScore(teacherScoreAnnotation.data.value, reportSettings);
        }
      }
    }
    return autoScoreAnnotation;
  }

  adjustKIScore(scoreValue: number, reportSettings: any) {
    const teacherScore = Math.round(scoreValue);
    const kiScoreBounds = this.getKIScoreBounds(reportSettings);
    let score = teacherScore;
    if (teacherScore > kiScoreBounds.max) {
      score = kiScoreBounds.max;
    }
    if (teacherScore < kiScoreBounds.min) {
      score = kiScoreBounds.min;
    }
    return score;
  }

  getKIScoreBounds(reportSettings: any) {
    const bounds = {
      min: 1,
      max: 5
    };
    if (reportSettings.customScoreValues && reportSettings.customScoreValues['ki']) {
      bounds.min = Math.min(...reportSettings.customScoreValues['ki']);
      bounds.max = Math.max(...reportSettings.customScoreValues['ki']);
    }
    return bounds;
  }

  addDataToAggregate(aggregate: any, annotation: any, reportSettings: any) {
    for (const subScore of annotation.data.scores) {
      if (aggregate[subScore.id] == null) {
        aggregate[subScore.id] = this.setupAggregateSubScore(subScore.id, reportSettings);
      }
      const subScoreVal = subScore.score;
      if (aggregate[subScore.id].counts[subScoreVal] > -1) {
        aggregate[subScore.id].counts[subScoreVal]++;
        aggregate[subScore.id].scoreSum += subScoreVal;
        aggregate[subScore.id].scoreCount++;
        aggregate[subScore.id].average =
          aggregate[subScore.id].scoreSum / aggregate[subScore.id].scoreCount;
      }
    }
    return aggregate;
  }

  setupAggregateSubScore(subScoreId: string, reportSettings: any) {
    let counts = {};
    if (reportSettings.customScoreValues && reportSettings.customScoreValues[subScoreId]) {
      counts = this.getCustomScoreValueCounts(reportSettings.customScoreValues[subScoreId]);
    } else {
      counts = this.getPossibleScoreValueCounts(subScoreId);
    }
    return {
      scoreSum: 0,
      scoreCount: 0,
      counts: counts,
      average: 0
    };
  }

  getCustomScoreValueCounts(scoreValues: any[]) {
    let counts = {};
    for (const value of scoreValues) {
      counts[value] = 0;
    }
    return counts;
  }

  getPossibleScoreValueCounts(subScoreId: string) {
    if (subScoreId === 'ki') {
      return {
        1: 0,
        2: 0,
        3: 0,
        4: 0,
        5: 0
      };
    } else {
      return {
        1: 0,
        2: 0,
        3: 0
      };
    }
  }

  processMilestoneGraphsAndData(content: any, aggregateAutoScores: any) {
    for (const componentAggregate of Object.values(aggregateAutoScores)) {
      let subScoreIndex = 0;
      for (let [subScoreId, aggregateData] of Object.entries(componentAggregate)) {
        let index = 0;
        if (subScoreId !== 'ki') {
          subScoreIndex++;
          index = subScoreIndex;
        }
        const data = JSON.stringify(aggregateData).replace(/\"/g, "'");
        const graphRegex = new RegExp(`milestone-report-graph{1,} id="(${subScoreId})"`, 'g');
        content = content.replace(graphRegex, `$& data=\"${data}\"`);
        const dataRegex = new RegExp(`milestone-report-data{1,} score-id="(${subScoreId})"`, 'g');
        content = content.replace(dataRegex, `$& data=\"${data}\"`);
      }
    }
    return content;
  }

  setReportAvailable(projectAchievement: any, reportAvailable: boolean) {
    projectAchievement.isReportAvailable = reportAvailable;
  }

  saveMilestone(milestone: any) {
    let index = -1;
    const projectAchievements = this.ProjectService.getAchievementItems();
    for (let i = 0; i < projectAchievements.length; i++) {
      if (projectAchievements[i].id === milestone.id) {
        index = i;
        projectAchievements[i] = milestone;
        break;
      }
    }
    if (index < 0) {
      if (projectAchievements && milestone) {
        projectAchievements.push(milestone);
      }
    }
    this.saveProject();
  }

  createMilestone() {
    let projectAchievements = this.ProjectService.getAchievementItems();
    if (projectAchievements != null) {
      // get the time of tomorrow at 3pm
      const tomorrow = moment()
        .add(1, 'days')
        .hours(23)
        .minutes(11)
        .seconds(59);
      return {
        id: this.AchievementService.getAvailableAchievementId(),
        name: '',
        description: '',
        type: 'milestone',
        params: {
          nodeIds: [],
          targetDate: tomorrow.valueOf()
        },
        icon: {
          image: ''
        },
        items: this.UtilService.makeCopyOfJSONObject(this.ProjectService.idToOrder),
        isVisible: true
      };
    }
    return null;
  }

  deleteMilestone(milestone: any) {
    const projectAchievements = this.ProjectService.getAchievementItems();
    let index = -1;
    for (let i = 0; i < projectAchievements.length; i++) {
      if (projectAchievements[i].id === milestone.id) {
        index = i;
        break;
      }
    }

    if (index > -1) {
      projectAchievements.splice(index, 1);
      this.saveProject();
    }
  }

  saveProject() {
    this.clearTempFields();
    this.ProjectService.saveProject();
  }

  clearTempFields() {
    const projectAchievements = this.ProjectService.getAchievementItems();
    for (const projectAchievement of projectAchievements) {
      this.workgroupsStorage.push(projectAchievement.workgroups);
      this.numberOfStudentsCompletedStorage.push(projectAchievement.numberOfStudentsCompleted);
      this.percentageCompletedStorage.push(projectAchievement.percentageCompleted);
      delete projectAchievement.items;
      delete projectAchievement.workgroups;
      delete projectAchievement.numberOfStudentsCompleted;
      delete projectAchievement.numberOfStudentsInRun;
      delete projectAchievement.percentageCompleted;
      delete projectAchievement.generatedReport;
      delete projectAchievement.generatedRecommendations;
      delete projectAchievement.nodeId;
      delete projectAchievement.componentId;
      delete projectAchievement.isReportAvailable;
    }
  }

  showMilestoneDetails(milestone: any, $event: any, hideStudentWork: boolean = false) {
    const title = this.getTranslation('MILESTONE_DETAILS_TITLE', {
      name: milestone.name
    });
    const template = `<md-dialog class="dialog--wider">
          <md-toolbar>
            <div class="md-toolbar-tools">
              <h2>${title}</h2>
            </div>
          </md-toolbar>
          <md-dialog-content class="gray-lighter-bg md-dialog-content">
            <milestone-details milestone="milestone"
                               hide-student-work="hideStudentWork"
                               on-show-workgroup="onShowWorkgroup(value)"
                               on-visit-node-grading="onVisitNodeGrading()"></milestone-details>
          </md-dialog-content>
          <md-dialog-actions layout="row" layout-align="start center">
            <span flex></span>
            <md-button class="md-primary"
                       ng-click="edit()"
                       ng-if="milestone.type !== 'milestoneReport'"
                       aria-label="{{ ::'EDIT' | translate }}">
              {{ ::'EDIT' | translate }}
            </md-button>
            <md-button class="md-primary"
                       ng-click="close()"
                       aria-label="{{ ::'CLOSE' | translate }}">
              {{ ::'CLOSE' | translate }}
            </md-button>
          </md-dialog-actions>
        </md-dialog>`;
    this.upgrade.$injector.get('$mdDialog')
      .show({
        parent: angular.element(document.body),
        template: template,
        ariaLabel: title,
        fullscreen: true,
        multiple: true,
        targetEvent: $event,
        clickOutsideToClose: true,
        escapeToClose: true,
        locals: {
          $event: $event,
          milestone: milestone,
          hideStudentWork: hideStudentWork
        },
        controller: [
          '$scope',
          '$state',
          '$mdDialog',
          'milestone',
          '$event',
          'TeacherDataService',
          function DialogController(
            $scope,
            $state,
            $mdDialog,
            milestone,
            $event,
            TeacherDataService
          ) {
            $scope.milestone = milestone;
            $scope.hideStudentWork = hideStudentWork;
            $scope.event = $event;
            $scope.close = function() {
              $scope.saveMilestoneClosedEvent();
              $mdDialog.hide();
            };
            $scope.edit = function() {
              $mdDialog.hide({
                milestone: $scope.milestone,
                action: 'edit',
                $event: $event
              });
            };
            $scope.onShowWorkgroup = function(workgroup: any) {
              $scope.saveMilestoneClosedEvent();
              $mdDialog.hide();
              TeacherDataService.setCurrentWorkgroup(workgroup);
              $state.go('root.nodeProgress');
            };
            $scope.onVisitNodeGrading = function() {
              $mdDialog.hide();
            };
            $scope.saveMilestoneOpenedEvent = function() {
              $scope.saveMilestoneEvent('MilestoneOpened');
            };
            $scope.saveMilestoneClosedEvent = function() {
              $scope.saveMilestoneEvent('MilestoneClosed');
            };
            $scope.saveMilestoneEvent = function(event: any) {
              const context = 'ClassroomMonitor',
                nodeId = null,
                componentId = null,
                componentType = null,
                category = 'Navigation',
                data = { milestoneId: $scope.milestone.id },
                projectId = null;
              TeacherDataService.saveEvent(
                context,
                nodeId,
                componentId,
                componentType,
                category,
                event,
                data,
                projectId
              );
            };
            $scope.saveMilestoneOpenedEvent();
          }
        ]
      })
      .then(
        data => {
          if (data && data.action && data.milestone) {
            if (data.action === 'edit') {
              let milestone = angular.copy(data.milestone);
              this.editMilestone(milestone, data.$event);
            }
          }
        },
        () => {}
      );
  }

  editMilestone(milestone: any, $event: any) {
    let editMode = milestone ? true : false;
    let title = editMode ? this.getTranslation('EDIT_MILESTONE') : this.getTranslation('ADD_MILESTONE');

    if (!editMode) {
      milestone = this.createMilestone();
    }

    let template = `<md-dialog class="dialog--wide">
          <md-toolbar>
            <div class="md-toolbar-tools">
              <h2>${title}</h2>
            </div>
          </md-toolbar>
          <md-dialog-content class="gray-lighter-bg md-dialog-content">
            <milestone-edit milestone="milestone" on-change="onChange(milestone, valid)"></milestone-edit>
          </md-dialog-content>
          <md-dialog-actions layout="row" layout-align="end center">
            <md-button ng-click="close()"
                       aria-label="{{ ::'CANCEL' | translate }}">
              {{ ::'CANCEL' | translate }}
            </md-button>
            <md-button class="md-warn"
                       ng-click="delete()"
                       aria-label="{{ ::'DELETE' | translate }}">
              {{ ::'DELETE' | translate }}
            </md-button>
            <md-button class="md-primary"
                       ng-click="save()"
                       aria-label="{{ ::'SAVE' | translate }}">
              {{ ::'SAVE' | translate }}
            </md-button>
          </md-dialog-actions>
        </md-dialog>`;

    // display the milestone edit form in a dialog
    this.upgrade.$injector.get('$mdDialog')
      .show({
        parent: angular.element(document.body),
        template: template,
        ariaLabel: title,
        fullscreen: true,
        targetEvent: $event,
        clickOutsideToClose: true,
        escapeToClose: true,
        locals: {
          editMode: editMode,
          $event: $event,
          milestone: milestone
        },
        controller: [
          '$scope',
          '$mdDialog',
          '$filter',
          'milestone',
          'editMode',
          '$event',
          function DialogController($scope: any, $mdDialog: any, $filter: any, milestone: any,
              editMode: boolean, $event: any) {
            $scope.editMode = editMode;
            $scope.milestone = milestone;
            $scope.$event = $event;
            $scope.valid = editMode;

            $scope.$translate = $filter('translate');

            $scope.close = function() {
              $mdDialog.hide({
                milestone: $scope.milestone,
                $event: $scope.$event
              });
            };

            $scope.save = function() {
              if ($scope.valid) {
                $mdDialog.hide({
                  milestone: $scope.milestone,
                  save: true,
                  $event: $scope.$event
                });
              } else {
                alert($scope.$translate('MILESTONE_EDIT_INVALID_ALERT'));
              }
            };

            $scope.delete = function() {
              $mdDialog.hide({
                milestone: $scope.milestone,
                delete: true,
                $event: $scope.$event
              });
            };

            $scope.onChange = function(milestone: any, valid: boolean) {
              $scope.milestone = milestone;
              $scope.valid = valid;
            };
          }
        ]
      })
      .then(
        data => {
          if (data) {
            if (data.milestone) {
              if (data.save) {
                this.saveMilestone(data.milestone);
              }
              if (data.delete) {
                this.deleteMilestoneConfirm(data.milestone, $event);
              }
            }
          }
        },
        () => {}
      );
  }

  deleteMilestoneConfirm(milestone: any, $event: any) {
    if (milestone) {
      const title = milestone.name;
      const label = this.getTranslation('DELETE_MILESTONE');
      const msg = this.getTranslation('DELETE_MILESTONE_CONFIRM', { name: milestone.name });
      const yes = this.getTranslation('YES');
      const cancel = this.getTranslation('CANCEL');

      const confirm = this.upgrade.$injector.get('$mdDialog')
        .confirm()
        .title(title)
        .textContent(msg)
        .ariaLabel(label)
        .targetEvent($event)
        .ok(yes)
        .cancel(cancel);

      this.upgrade.$injector.get('$mdDialog').show(confirm).then(
        () => {
          this.deleteMilestone(milestone);
        },
        () => {}
      );
    }
  }
}
