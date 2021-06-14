'use strict';

import { TeacherProjectService } from '../../services/teacherProjectService';
import { UtilService } from '../../services/utilService';

class MilestonesAuthoringController {
  $translate: any;
  project: any;
  idToOrder: any;
  nodeIds: any;
  milestoneIds: any;
  milestoneSatisfyCriteriaIds: any;
  reportIds: any;
  templateIds: any;
  templateSatisfyCriteriaIds: any;
  idToExpanded: any = {};
  availableSatisfyCriteria: any[] = [{ value: 'isCompleted', text: 'Is Completed' }];
  availableSatisfyCriteriaFunctions: any[];

  milestoneIdPrefix: string = 'milestone-';
  milestoneSatisfyCriteriaIdPrefix: string = 'milestone-satisfy-criteria-';
  reportIdPrefix: string = 'report-';
  templateIdPrefix: string = 'template-';
  templateSatisfyCriteriaIdPrefix: string = 'template-satisfy-criteria-';

  customScoreKey: string;
  customScoreValues: string;

  static $inject = ['$filter', 'ProjectService', 'UtilService'];

  constructor(
    private $filter: any,
    private ProjectService: TeacherProjectService,
    private UtilService: UtilService
  ) {
    this.$translate = this.$filter('translate');
    this.availableSatisfyCriteriaFunctions = [
      {
        value: 'percentOfScoresLessThan',
        text: this.$translate('percentOfScoresLessThan')
      },
      {
        value: 'percentOfScoresLessThanOrEqualTo',
        text: this.$translate('percentOfScoresLessThanOrEqualTo')
      },
      {
        value: 'percentOfScoresGreaterThan',
        text: this.$translate('percentOfScoresGreaterThan')
      },
      {
        value: 'percentOfScoresGreaterThanOrEqualTo',
        text: this.$translate('percentOfScoresGreaterThanOrEqualTo')
      },
      {
        value: 'percentOfScoresEqualTo',
        text: this.$translate('percentOfScoresEqualTo')
      }
    ];
  }

  $onInit() {
    this.project = this.ProjectService.project;
    this.idToOrder = this.ProjectService.idToOrder;
    this.nodeIds = this.getStepNodeIds();
    if (this.project.achievements == null) {
      this.initializeMilestones();
    }
    this.milestoneIds = this.getMilestoneIds();
    this.milestoneSatisfyCriteriaIds = this.getMilestoneSatisfyCriteriaIds();
    this.reportIds = this.getReportIds();
    this.templateIds = this.getTemplateIds();
    this.templateSatisfyCriteriaIds = this.getTemplateSatisfyCriteriaIds();
    this.populateIdToExpanded();
  }

  initializeMilestones() {
    this.project.achievements = {
      isEnabled: false,
      items: []
    };
  }

  populateIdToExpanded() {
    for (const milestone of this.project.achievements.items) {
      this.idToExpanded[milestone.id] = false;
      for (const template of milestone.report.templates) {
        this.idToExpanded[template.id] = true;
      }
    }
  }

  isApplicationNode(nodeId) {
    return this.ProjectService.isApplicationNode(nodeId);
  }

  getNodePositionAndTitleByNodeId(nodeId) {
    return this.ProjectService.getNodePositionAndTitleByNodeId(nodeId);
  }

  getComponentsByNodeId(nodeId) {
    return this.ProjectService.getComponentsByNodeId(nodeId);
  }

  getStepNodeIds() {
    this.ProjectService.idToOrder;
  }

  createMilestone() {
    const item = {
      id: this.generateUniqueId(this.milestoneIdPrefix, this.milestoneIds),
      isEnabled: true,
      type: 'milestoneReport',
      name: '',
      description: '',
      icon: {
        image: ''
      },
      report: this.createReport(),
      satisfyCriteria: [],
      satisfyMinPercentage: 50,
      satisfyMinNumWorkgroups: 2,
      satisfyConditional: 'all'
    };
    return item;
  }

  getMilestoneIds() {
    const milestoneIds = {};
    for (const milestone of this.project.achievements.items) {
      milestoneIds[milestone.id] = true;
    }
    return milestoneIds;
  }

  addMilestone(index) {
    const milestone = this.createMilestone();
    this.project.achievements.items.splice(index, 0, milestone);
    this.milestoneIds[milestone.id] = true;
    this.addToIdToExpanded(milestone.id);
    this.save();
    return milestone;
  }

  deleteMilestone(index) {
    const message = this.$translate('areYouSureYouWantToDeleteMilestoneX', {
      milestoneNumber: index + 1
    });
    if (confirm(message)) {
      const deletedMilestones = this.project.achievements.items.splice(index, 1);
      const deletedMilestone = deletedMilestones[0];
      delete this.milestoneIds[deletedMilestone.id];
      this.deleteFromIdToExpanded(deletedMilestone.id);
      this.save();
      return deletedMilestone;
    } else {
      return null;
    }
  }

  createMilestoneSatisfyCriteria() {
    return {
      id: this.generateUniqueId(
        this.milestoneSatisfyCriteriaIdPrefix,
        this.milestoneSatisfyCriteriaIds
      ),
      nodeId: '',
      componentId: '',
      name: ''
    };
  }

  getMilestoneSatisfyCriteriaIds() {
    const milestoneSatisfyCriteriaIds = {};
    for (const milestone of this.project.achievements.items) {
      for (const milestoneSatisfyCriteria of milestone.satisfyCriteria) {
        milestoneSatisfyCriteriaIds[milestoneSatisfyCriteria.id] = true;
      }
    }
    return milestoneSatisfyCriteriaIds;
  }

  generateMilestoneSatisfyCriteriaId() {
    return this.milestoneSatisfyCriteriaIdPrefix + this.UtilService.generateKey(10);
  }

  isUniqueMilestoneSatisfyCriteriaId(id) {
    return this.milestoneSatisfyCriteriaIds[id] == null;
  }

  addMilestoneSatisfyCriteria(milestone, index) {
    const milestoneSatisfyCriteria = this.createMilestoneSatisfyCriteria();
    milestone.satisfyCriteria.splice(index, 0, milestoneSatisfyCriteria);
    this.milestoneSatisfyCriteriaIds[milestoneSatisfyCriteria.id] = true;
    this.save();
    return milestoneSatisfyCriteria;
  }

  deleteMilestoneSatisfyCriteria(milestone, index) {
    const message = this.$translate('areYouSureYouWantToDeleteMilestoneSatisfyCriteriaX', {
      milestoneSatisfyCriteriaNumber: index + 1
    });
    if (confirm(message)) {
      const deletedMilestoneSatisfyCriterias = milestone.satisfyCriteria.splice(index, 1);
      const deletedMilestoneSatisfyCriteria = deletedMilestoneSatisfyCriterias[0];
      delete this.milestoneSatisfyCriteriaIds[deletedMilestoneSatisfyCriteria.id];
      this.save();
      return deletedMilestoneSatisfyCriteria;
    } else {
      return null;
    }
  }

  copySatisfyCriteriaToMilestone(milestone: any, nodeId: string, componentId: string): void {
    const message = this.$translate(
      'areYouSureYouWantToCopyTheNodeIdAndComponentIdToTheRestOfThisMilestone'
    );
    if (confirm(message)) {
      this.setNodeIdAndComponentIdToAllSatisfyCriteria(milestone, nodeId, componentId);
      this.setNodeIdAndComponentIdToAllLocations(milestone, nodeId, componentId);
      this.save();
    }
  }

  setNodeIdAndComponentIdToAllSatisfyCriteria(
    milestone: any,
    nodeId: string,
    componentId: string
  ): void {
    for (const template of milestone.report.templates) {
      for (const satisfyCriteria of template.satisfyCriteria) {
        satisfyCriteria.nodeId = nodeId;
        satisfyCriteria.componentId = componentId;
      }
    }
  }

  setNodeIdAndComponentIdToAllLocations(milestone: any, nodeId: string, componentId: string): void {
    for (const location of milestone.report.locations) {
      location.nodeId = nodeId;
      location.componentId = componentId;
    }
  }

  createReport() {
    const report = {
      id: this.generateUniqueId(this.reportIdPrefix, this.reportIds),
      title: '',
      isEnabled: true,
      audience: ['teacher'],
      templates: [],
      locations: [
        {
          nodeId: '',
          componentId: ''
        }
      ],
      customScoreValues: {}
    };
    return report;
  }

  getReportIds() {
    const reportIds = {};
    for (const milestone of this.project.achievements.items) {
      reportIds[milestone.report.id] = true;
    }
    return reportIds;
  }

  generateUniqueId(prefix: string, existingIds: any[]): string {
    let id: string;
    do {
      id = prefix + this.UtilService.generateKey(10);
    } while (existingIds[id] != null);
    return id;
  }

  createLocation() {
    return {
      nodeId: '',
      componentId: ''
    };
  }

  addLocation(report, index) {
    const location = this.createLocation();
    report.locations.splice(index, 0, location);
    this.save();
    return location;
  }

  deleteLocation(report, index) {
    if (confirm(this.$translate('areYouSureYouWantToDeleteThisLocation'))) {
      const deletedLocations = report.locations.splice(index, 1);
      const deletedLocation = deletedLocations[0];
      this.save();
      return deletedLocation;
    } else {
      return null;
    }
  }

  addCustomScoreValues(report, key, values) {
    if (this.validateCustomScoreValues(key, values)) {
      if (report.customScoreValues == null) {
        report.customScoreValues = {};
      }
      report.customScoreValues[key] = this.getNumberArrayFromCustomScoreValues(values);
      this.customScoreKey = '';
      this.customScoreValues = '';
      this.save();
    }
  }

  validateCustomScoreValues(key, values) {
    let errorMessage = '';
    if (key === '') {
      errorMessage += this.$translate('errorKeyMustNotBeEmpty') + '\n';
    }
    if (values === '') {
      errorMessage += this.$translate('errorValuesMustNotBeEmpty');
    }
    if (errorMessage === '') {
      return true;
    } else {
      alert(errorMessage);
      return false;
    }
  }

  getNumberArrayFromCustomScoreValues(valuesString) {
    const numberArray = [];
    for (const value of valuesString.split(',')) {
      const numberValue = parseInt(value);
      if (!isNaN(numberValue)) {
        numberArray.push(numberValue);
      }
    }
    return numberArray;
  }

  deleteCustomScoreValues(report, key) {
    if (confirm(this.$translate('areYouSureYouWantToDeleteThisCustomScoreValue'))) {
      delete report.customScoreValues[key];
      this.save();
    }
  }

  createTemplate() {
    return {
      id: this.generateUniqueId(this.templateIdPrefix, this.templateIds),
      description: '',
      recommendations: '',
      content: '',
      satisfyConditional: '',
      satisfyCriteria: []
    };
  }

  getTemplateIds() {
    const templateIds = {};
    for (const milestone of this.project.achievements.items) {
      for (const template of milestone.report.templates) {
        templateIds[template.id] = true;
      }
    }
    return templateIds;
  }

  addTemplate(report, index) {
    const template = this.createTemplate();
    report.templates.splice(index, 0, template);
    this.templateIds[template.id] = true;
    this.addToIdToExpanded(template.id);
    this.save();
    return template;
  }

  deleteTemplate(report, index) {
    const message = this.$translate('areYouSureYouWantToDeleteTemplateX', {
      templateNumber: index + 1
    });
    if (confirm(message)) {
      const deletedTemplates = report.templates.splice(index, 1);
      const deletedTemplate = deletedTemplates[0];
      delete this.templateIds[deletedTemplate.id];
      this.deleteFromIdToExpanded(deletedTemplate.id);
      this.save();
      return deletedTemplate;
    } else {
      return null;
    }
  }

  createTemplateSatisfyCriteria() {
    return {
      id: this.generateUniqueId(
        this.templateSatisfyCriteriaIdPrefix,
        this.templateSatisfyCriteriaIds
      ),
      nodeId: '',
      componentId: '',
      percentThreshold: 50,
      targetVariable: '',
      function: '',
      type: 'autoScore',
      value: 3
    };
  }

  getTemplateSatisfyCriteriaIds() {
    const templateSatisfyCriteriaIds = {};
    for (const milestone of this.project.achievements.items) {
      for (const template of milestone.report.templates) {
        for (const satisfyCriteria of template.satisfyCriteria) {
          templateSatisfyCriteriaIds[satisfyCriteria.id] = true;
        }
      }
    }
    return templateSatisfyCriteriaIds;
  }

  addTemplateSatisfyCriteria(template, index) {
    const satisfyCriteria = this.createTemplateSatisfyCriteria();
    template.satisfyCriteria.splice(index, 0, satisfyCriteria);
    this.templateSatisfyCriteriaIds[satisfyCriteria.id] = true;
    this.save();
    return satisfyCriteria;
  }

  deleteTemplateSatisfyCriteria(template, index) {
    const message = this.$translate('areYouSureYouWantToDeleteTemplateSatisfyCriteriaX', {
      templateSatisfyCriteriaNumber: index + 1
    });
    if (confirm(message)) {
      const deletedSatisfyCriteria = template.satisfyCriteria.splice(index, 1);
      const deletedSatisfyCriterion = deletedSatisfyCriteria[0];
      delete this.templateSatisfyCriteriaIds[deletedSatisfyCriterion.id];
      this.save();
      return deletedSatisfyCriterion;
    } else {
      return null;
    }
  }

  addToIdToExpanded(id) {
    this.idToExpanded[id] = true;
  }

  deleteFromIdToExpanded(id) {
    delete this.idToExpanded[id];
  }

  expand(id) {
    this.idToExpanded[id] = true;
  }

  collapse(id) {
    this.idToExpanded[id] = false;
  }

  save() {
    this.ProjectService.saveProject();
  }
}

export default MilestonesAuthoringController;
