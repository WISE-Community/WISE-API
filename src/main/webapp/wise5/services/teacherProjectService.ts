'use strict';
import * as angular from 'angular';
import * as $ from 'jquery';
import { ProjectService } from '../services/projectService';
import { ConfigService } from '../services/configService';
import { UtilService } from '../services/utilService';
import { Injectable } from '@angular/core';
import { UpgradeModule } from '@angular/upgrade/static';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { SessionService } from './sessionService';

@Injectable()
export class TeacherProjectService extends ProjectService {

  private nodeChangedSource: Subject<boolean> = new Subject<boolean>();
  public nodeChanged$: Observable<boolean> = this.nodeChangedSource.asObservable();
  private refreshProjectSource: Subject<void> = new Subject<void>();
  public refreshProject$ = this.refreshProjectSource.asObservable();
  private scrollToBottomOfPageSource: Subject<void> = new Subject<void>();
  public scrollToBottomOfPage$ = this.scrollToBottomOfPageSource.asObservable();
  private showAdvancedComponentViewSource: Subject<any> = new Subject<any>();
  public showAdvancedComponentView$: Observable<any> =
      this.showAdvancedComponentViewSource.asObservable();

  constructor(
      protected upgrade: UpgradeModule,
      protected http: HttpClient,
      protected ConfigService: ConfigService,
      protected SessionService: SessionService,
      protected UtilService: UtilService) {
    super(upgrade, http, ConfigService, SessionService, UtilService);
  }

  getNewProjectTemplate() {
    return {
      nodes: [
        {
          id: 'group0',
          type: 'group',
          title: 'Master',
          startId: 'group1',
          ids: ['group1']
        },
        {
          id: 'group1',
          type: 'group',
          title: this.UtilService.translate('FIRST_ACTIVITY'),
          startId: 'node1',
          ids: ['node1'],
          icons: {
            default: {
              color: '#2196F3',
              type: 'font',
              fontSet: 'material-icons',
              fontName: 'info'
            }
          }
        },
        {
          id: 'node1',
          type: 'node',
          title: this.UtilService.translate('FIRST_STEP'),
          components: [],
          constraints: [],
          showSaveButton: false,
          showSubmitButton: false,
          transitionLogic: {
            transitions: []
          }
        }
      ],
      constraints: [],
      startGroupId: 'group0',
      startNodeId: 'node1',
      navigationMode: 'guided',
      layout: {
        template: 'starmap|leftNav|rightNav'
      },
      metadata: {
        title: ''
      },
      notebook: {
        enabled: false,
        label: this.UtilService.translate('NOTEBOOK'),
        enableAddNew: true,
        itemTypes: {
          note: {
            type: 'note',
            enabled: true,
            enableLink: true,
            enableAddNote: true,
            enableClipping: true,
            enableStudentUploads: true,
            requireTextOnEveryNote: false,
            label: {
              singular: this.UtilService.translate('NOTE_LOWERCASE'),
              plural: this.UtilService.translate('NOTES_LOWERCASE'),
              link: this.UtilService.translate('NOTES'),
              icon: 'note',
              color: '#1565C0'
            }
          },
          report: {
            enabled: false,
            label: {
              singular: this.UtilService.translate('REPORT_LOWERCASE'),
              plural: this.UtilService.translate('REPORTS_LOWERCASE'),
              link: this.UtilService.translate('REPORT'),
              icon: 'assignment',
              color: '#AD1457'
            },
            notes: [
              {
                reportId: 'finalReport',
                title: this.UtilService.translate('FINAL_REPORT'),
                description: this.UtilService.translate('REPORT_DESCRIPTION'),
                prompt: this.UtilService.translate('REPORT_PROMPT'),
                content: this.UtilService.translate('REPORT_CONTENT')
              }
            ]
          }
        }
      },
      teacherNotebook: {
        enabled: true,
        label: this.UtilService.translate('TEACHER_NOTEBOOK'),
        enableAddNew: true,
        itemTypes: {
          note: {
            type: 'note',
            enabled: false,
            enableLink: true,
            enableAddNote: true,
            enableClipping: true,
            enableStudentUploads: true,
            requireTextOnEveryNote: false,
            label: {
              singular: this.UtilService.translate('NOTE_LOWERCASE'),
              plural: this.UtilService.translate('NOTES_LOWERCASE'),
              link: this.UtilService.translate('NOTES'),
              icon: 'note',
              color: '#1565C0'
            }
          },
          report: {
            enabled: true,
            label: {
              singular: this.UtilService.translate('TEACHER_REPORT_LOWERCASE'),
              plural: this.UtilService.translate('TEACHER_REPORTS_LOWERCASE'),
              link: this.UtilService.translate('TEACHER_REPORT'),
              icon: 'assignment',
              color: '#AD1457'
            },
            notes: [
              {
                reportId: 'teacherReport',
                title: this.UtilService.translate('TEACHER_REPORT'),
                description: this.UtilService.translate('TEACHER_REPORT_DESCRIPTION'),
                prompt: this.UtilService.translate('TEACHER_REPORT_PROMPT'),
                content: this.UtilService.translate('TEACHER_REPORT_CONTENT')
              }
            ]
          }
        }
      },
      inactiveNodes: []
    };
  }

  notifyAuthorProjectBeginEnd(projectId, isBegin) {
    return this.http.post(
        `${this.ConfigService.getConfigParam('notifyAuthoringBeginEndURL')}/${projectId}/${isBegin}`,
        null).toPromise();
  }

  notifyAuthorProjectBegin(projectId) {
    return this.notifyAuthorProjectBeginEnd(projectId, true);
  }

  notifyAuthorProjectEnd(projectId = null) {
    return this.upgrade.$injector.get('$q')((resolve, reject) => {
      if (projectId == null) {
        if (this.project != null) {
          projectId = this.ConfigService.getProjectId();
        } else {
          resolve();
        }
      }
      this.notifyAuthorProjectBeginEnd(projectId, false).then(() => {
        resolve();
      });
    });
  }

  copyProject(projectId) {
    return this.http.post(`${this.ConfigService.getConfigParam('copyProjectURL')}/${projectId}`,
        null)
      .toPromise()
      .then(newProject => {
        return newProject;
      });
  }

  /**
   * Registers a new project having the projectJSON content with the server.
   * Returns a new project id if the project is successfully registered.
   * @param projectJSONString a valid JSON string
   */
  registerNewProject(projectName, projectJSONString) {
    return this.http
        .post(this.ConfigService.getConfigParam('registerNewProjectURL'), {
          projectName: projectName,
          projectJSONString: projectJSONString
        })
        .toPromise()
        .then( newProjectId => {
          return newProjectId;
        });
  }

  /**
   * Replace a component
   * @param nodeId the node id
   * @param componentId the component id
   * @param component the new component
   */
  replaceComponent(nodeId, componentId, component) {
    const components = this.getComponentsByNodeId(nodeId);
    for (let c = 0; c < components.length; c++) {
      if (components[c].id === componentId) {
        components[c] = component;
        break;
      }
    }
  }

  /**
   * Create a new group
   * @param title the title of the group
   * @returns the group object
   */
  createGroup(title) {
    return {
      id: this.getNextAvailableGroupId(),
      type: 'group',
      title: title,
      startId: '',
      constraints: [],
      transitionLogic: {
        transitions: []
      },
      ids: []
    };
  }

  /**
   * Create a new node
   * @param title the title of the node
   * @returns the node object
   */
  createNode(title) {
    return {
      id: this.getNextAvailableNodeId(),
      title: title,
      type: 'node',
      constraints: [],
      transitionLogic: {
        transitions: []
      },
      showSaveButton: false,
      showSubmitButton: false,
      components: []
    };
  }

  /**
   * Move nodes inside a group node
   * @param nodeIds the node ids to move
   * @param nodeId the node id of the group we are moving the nodes inside
   */
  moveNodesInside(nodeIds, nodeId) {
    const movedNodes = [];

    for (let n = 0; n < nodeIds.length; n++) {
      const tempNodeId = nodeIds[n];
      const tempNode = this.getNodeById(tempNodeId);
      movedNodes.push(tempNode);

      const movingNodeIsActive = this.isActive(tempNodeId);
      const stationaryNodeIsActive = this.isActive(nodeId);

      if (movingNodeIsActive && stationaryNodeIsActive) {
        this.removeNodeIdFromTransitions(tempNodeId);
        this.removeNodeIdFromGroups(tempNodeId);

        if (n == 0) {
          /*
           * this is the first node we are moving so we will insert it
           * into the beginning of the group
           */
          this.insertNodeInsideOnlyUpdateTransitions(tempNodeId, nodeId);
          this.insertNodeInsideInGroups(tempNodeId, nodeId);
        } else {
          /*
           * this is not the first node we are moving so we will insert
           * it after the node we previously inserted
           */
          this.insertNodeAfterInTransitions(tempNode, nodeId);
          this.insertNodeAfterInGroups(tempNodeId, nodeId);
        }
      } else if (movingNodeIsActive && !stationaryNodeIsActive) {
        this.removeNodeIdFromTransitions(tempNodeId);
        this.removeNodeIdFromGroups(tempNodeId);

        if (n == 0) {
          /*
           * this is the first node we are moving so we will insert it
           * into the beginning of the group
           */
          this.moveFromActiveToInactiveInsertInside(tempNode, nodeId);
        } else {
          /*
           * this is not the first node we are moving so we will insert
           * it after the node we previously inserted
           */
          this.moveToInactive(tempNode, nodeId);
        }
      } else if (!movingNodeIsActive && stationaryNodeIsActive) {
        this.moveToActive(tempNode);

        if (n == 0) {
          /*
           * this is the first node we are moving so we will insert it
           * into the beginning of the group
           */
          this.insertNodeInsideOnlyUpdateTransitions(tempNodeId, nodeId);
          this.insertNodeInsideInGroups(tempNodeId, nodeId);
        } else {
          /*
           * this is not the first node we are moving so we will insert
           * it after the node we previously inserted
           */
          this.insertNodeAfterInTransitions(tempNode, nodeId);
          this.insertNodeAfterInGroups(tempNodeId, nodeId);
        }
      } else if (!movingNodeIsActive && !stationaryNodeIsActive) {
        this.removeNodeIdFromTransitions(tempNodeId);
        this.removeNodeIdFromGroups(tempNodeId);

        if (n == 0) {
          /*
           * this is the first node we are moving so we will insert it
           * into the beginning of the group
           */
          this.moveFromInactiveToInactiveInsertInside(tempNode, nodeId);
        } else {
          /*
           * this is not the first node we are moving so we will insert
           * it after the node we previously inserted
           */
          this.moveInactiveNodeToInactiveSection(tempNode, nodeId);
        }
      }

      /*
       * remember the node id so we can put the next node (if any)
       * after this one
       */
      nodeId = tempNode.id;
    }
    return movedNodes;
  }

  /**
   * Move nodes after a certain node id
   * @param nodeIds the node ids to move
   * @param nodeId the node id we will put the moved nodes after
   */
  moveNodesAfter(nodeIds, nodeId) {
    const movedNodes = [];

    for (let tempNodeId of nodeIds) {
      const node = this.getNodeById(tempNodeId);
      movedNodes.push(node);

      const movingNodeIsActive = this.isActive(tempNodeId);
      const stationaryNodeIsActive = this.isActive(nodeId);

      if (movingNodeIsActive && stationaryNodeIsActive) {
        this.removeNodeIdFromTransitions(tempNodeId);
        this.removeNodeIdFromGroups(tempNodeId);
        this.insertNodeAfterInGroups(tempNodeId, nodeId);
        this.insertNodeAfterInTransitions(node, nodeId);
      } else if (movingNodeIsActive && !stationaryNodeIsActive) {
        this.removeNodeIdFromTransitions(tempNodeId);
        this.removeNodeIdFromGroups(tempNodeId);
        this.moveToInactive(node, nodeId);
      } else if (!movingNodeIsActive && stationaryNodeIsActive) {
        this.moveToActive(node);
        this.insertNodeAfterInGroups(tempNodeId, nodeId);
        this.insertNodeAfterInTransitions(node, nodeId);
      } else if (!movingNodeIsActive && !stationaryNodeIsActive) {
        this.removeNodeIdFromTransitions(tempNodeId);
        this.removeNodeIdFromGroups(tempNodeId);
        this.moveInactiveNodeToInactiveSection(node, nodeId);
      }

      // remember the node id so we can put the next node (if any) after this one
      nodeId = node.id;
    }
    return movedNodes;
  }

  /**
   * Copy nodes and put them after a certain node id
   * @param nodeIds the node ids to copy
   * @param nodeId the node id we will put the copied nodes after
   */
  copyNodesInside(nodeIds, nodeId) {
    const newNodes = [];
    for (let n = 0; n < nodeIds.length; n++) {
      const newNode = this.copyNode(nodeIds[n]);
      const newNodeId = newNode.id;
      if (n == 0) {
        this.createNodeInside(newNode, nodeId);
      } else {
        this.createNodeAfter(newNode, nodeId);
      }
      nodeId = newNodeId;
      this.parseProject();
      newNodes.push(newNode);
    }
    return newNodes;
  }

  /**
   * Copy the nodes into the project
   * @param selectedNodes the nodes to import
   * @param fromProjectId copy the nodes from this project
   * @param toProjectId copy the nodes into this project
   * @param nodeIdToInsertInsideOrAfter If this is a group, we will make the
   * new step the first step in the group. If this is a step, we will place
   * the new step after it.
   */
  copyNodes(selectedNodes, fromProjectId, toProjectId, nodeIdToInsertInsideOrAfter) {
    /*
     * Make the request to import the steps. This will copy the asset files
     * and change file names if necessary. If an asset file with the same
     * name exists in both projects we will check if their content is the
     * same. If the content is the same we don't need to copy the file. If
     * the content is different, we need to make a copy of the file with a
     * new name and change all the references in the steps to use the new
     * name.
     */
    return this.http.post(this.ConfigService.getConfigParam('importStepsURL'),
        {
          steps: angular.toJson(selectedNodes),
          fromProjectId: fromProjectId,
          toProjectId: toProjectId
        })
        .toPromise()
        .then((selectedNodes: any) => {
      const inactiveNodes = this.getInactiveNodes();
      const newNodes = [];
      const newNodeIds = [];
      for (const selectedNode of selectedNodes) {
        const tempNode = this.UtilService.makeCopyOfJSONObject(selectedNode);
        if (this.isNodeIdUsed(tempNode.id)) {
          const nextAvailableNodeId = this.getNextAvailableNodeId(newNodeIds);
          tempNode.id = nextAvailableNodeId;
        }
        const tempComponents = tempNode.components;
        for (const tempComponent of tempComponents) {
          if (this.isComponentIdUsed(tempComponent.id)) {
            // we are already using the component id so we will need to change it
            tempComponent.id = this.getUnusedComponentId();
          }
        }
        tempNode.constraints = [];
        newNodes.push(tempNode);
        newNodeIds.push(tempNode.id);
      }

      if (nodeIdToInsertInsideOrAfter == null) {
        /*
         * the place to put the new node has not been specified so we
         * will place it in the inactive steps section
         */

        /*
         * Insert the node after the last inactive node. If there
         * are no inactive nodes it will just be placed in the
         * inactive nodes section. In the latter case we do this by
         * setting nodeIdToInsertInsideOrAfter to 'inactiveSteps'.
         */
        if (inactiveNodes != null && inactiveNodes.length > 0) {
          nodeIdToInsertInsideOrAfter = inactiveNodes[inactiveNodes.length - 1];
        } else {
          nodeIdToInsertInsideOrAfter = 'inactiveSteps';
        }
      }

      for (const newNode of newNodes) {
        if (this.isGroupNode(nodeIdToInsertInsideOrAfter)) {
          this.createNodeInside(newNode, nodeIdToInsertInsideOrAfter);
        } else {
          this.createNodeAfter(newNode, nodeIdToInsertInsideOrAfter);
        }

        /*
         * Update the nodeIdToInsertInsideOrAfter so that when we are
         * importing multiple steps, the steps get placed in the correct
         * order.
         *
         * Example
         * We are importing nodeA and nodeB and want to place them after
         * nodeX. Therefore we want the order to be
         *
         * nodeX
         * nodeA
         * nodeB
         *
         * This means after we add nodeA, we must update
         * nodeIdToInsertInsideOrAfter to be nodeA so that when we add
         * nodeB, it will be placed after nodeA.
         */
        nodeIdToInsertInsideOrAfter = newNode.id;
      }
      return newNodes;
    });
  }

  /**
   * Create a node inside the group
   * @param node the new node
   * @param nodeId the node id of the group to create the node in
   */
  createNodeInside(node, nodeId) {
    if (nodeId === 'inactiveNodes' || nodeId === 'inactiveGroups') {
      this.addInactiveNodeInsertAfter(node);
      this.setIdToNode(node.id, node);
    } else {
      this.setIdToNode(node.id, node);
      if (this.isInactive(nodeId)) {
        this.addInactiveNodeInsertInside(node, nodeId);
      } else {
        this.addNode(node);
        this.insertNodeInsideOnlyUpdateTransitions(node.id, nodeId);
        this.insertNodeInsideInGroups(node.id, nodeId);
      }
    }
  }

  /**
   * Create a node after the given node id
   * @param node the new node
   * @param nodeId the node to add after
   */
  createNodeAfter(newNode, nodeId) {
    if (this.isInactive(nodeId)) {
      this.addInactiveNodeInsertAfter(newNode, nodeId);
      this.setIdToNode(newNode.id, newNode);
    } else {
      this.addNode(newNode);
      this.setIdToNode(newNode.id, newNode);
      this.insertNodeAfterInGroups(newNode.id, nodeId);
      this.insertNodeAfterInTransitions(newNode, nodeId);
    }
  }

  /**
   * Copy nodes and put them after a certain node id
   * @param nodeIds the node ids to copy
   * @param nodeId the node id we will put the copied nodes after
   */
  copyNodesAfter(nodeIds, nodeId) {
    const newNodes = [];
    for (const nodeIdToCopy of nodeIds) {
      const newNode = this.copyNode(nodeIdToCopy);
      const newNodeId = newNode.id;
      this.createNodeAfter(newNode, nodeId);
      nodeId = newNodeId; // remember the node id so we can put the next node (if any) after this one
      this.parseProject();
      newNodes.push(newNode);
    }
    return newNodes;
  }

  isInactive(nodeId) {
    for (const inactiveNode of this.getInactiveNodes()) {
      if (inactiveNode.id === nodeId) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check if a node id is already being used in the project
   * @param nodeId check if this node id is already being used in the project
   * @return whether the node id is already being used in the project
   */
  isNodeIdUsed(nodeId) {
    for (const node of this.getNodes().concat(this.getInactiveNodes())) {
      if (node.id === nodeId) {
        return true;
      }
    }
    return false;
  }

  /**
   * Set a field in the transition logic of a node
   */
  setTransitionLogicField(nodeId, field, value) {
    const node = this.getNodeById(nodeId);
    const transitionLogic = node.transitionLogic;
    if (transitionLogic != null) {
      transitionLogic[field] = value;
    }
  }

  /**
   * Set the transition to value of a node
   * @param fromNodeId the from node
   * @param toNodeId the to node
   */
  setTransition(fromNodeId, toNodeId) {
    const node = this.getNodeById(fromNodeId);
    const transitionLogic = node.transitionLogic;
    if (transitionLogic != null) {
      let transitions = transitionLogic.transitions;
      if (transitions == null || transitions.length == 0) {
        transitionLogic.transitions = [];
        const transition = {};
        transitionLogic.transitions.push(transition);
        transitions = transitionLogic.transitions;
      }

      if (transitions != null && transitions.length > 0) {
        // get the first transition. we will assume there is only one transition.
        const transition = transitions[0];
        if (transition != null) {
          transition.to = toNodeId;
        }
      }
    }
  }

  /**
   * Get the node id that comes after a given node id
   * @param nodeId get the node id that comes after this node id
   * @return the node id that comes after the one that is passed in as a parameter, or null
   * if this is the last node in the sequence
   */
  getNodeIdAfter(nodeId) {
    const order = this.getOrderById(nodeId);
    if (order != null) {
      return this.getNodeIdByOrder(order + 1);
    } else {
      return null;
    }
  }

  /**
   * Add branch path taken constraints to the node
   * @param targetNodeId the node to add the constraints to
   * @param fromNodeId the from node id of the branch path taken constraint
   * @param toNodeId the to node id of the branch path taken constraint
   */
  addBranchPathTakenConstraints(targetNodeId, fromNodeId, toNodeId) {
    const node = this.getNodeById(targetNodeId);
    const makeThisNodeNotVisibleConstraint = {
      id: this.getNextAvailableConstraintIdForNodeId(targetNodeId),
      action: 'makeThisNodeNotVisible',
      targetId: targetNodeId,
      removalConditional: 'all',
      removalCriteria: [
        {
          name: 'branchPathTaken',
          params: {
            fromNodeId: fromNodeId,
            toNodeId: toNodeId
          }
        }
      ]
    };
    node.constraints.push(makeThisNodeNotVisibleConstraint);
    const makeThisNodeNotVisitableConstraint = {
      id: this.getNextAvailableConstraintIdForNodeId(targetNodeId),
      action: 'makeThisNodeNotVisitable',
      targetId: targetNodeId,
      removalConditional: 'all',
      removalCriteria: [
        {
          name: 'branchPathTaken',
          params: {
            fromNodeId: fromNodeId,
            toNodeId: toNodeId
          }
        }
      ]
    };
    node.constraints.push(makeThisNodeNotVisitableConstraint);
  }

  setProjectRubric(html) {
    this.project.rubric = html;
  }

  /**
   * Get the number of branch paths. This is assuming the node is a branch point.
   * @param nodeId The node id of the branch point node.
   * @return The number of branch paths for this branch point.
   */
  getNumberOfBranchPaths(nodeId) {
    const transitions = this.getTransitionsByFromNodeId(nodeId);
    if (transitions != null) {
      return transitions.length;
    }
    return 0;
  }

  /**
   * If this step is a branch point, we will return the criteria that is used
   * to determine which path the student gets assigned to.
   * @param nodeId The node id of the branch point.
   * @returns A human readable string containing the criteria of how students
   * are assigned branch paths on this branch point.
   */
  getBranchCriteriaDescription(nodeId) {
    const transitionLogic = this.getTransitionLogicByFromNodeId(nodeId);
    for (const transition of transitionLogic.transitions) {
      if (transition.criteria != null && transition.criteria.length > 0) {
        for (const singleCriteria of transition.criteria) {
          if (singleCriteria.name === 'choiceChosen') {
            return 'multiple choice';
          } else if (singleCriteria.name === 'score') {
            return 'score';
          }
        }
      }
    }

    /*
     * None of the transitions had a specific criteria so the branching is just
     * based on the howToChooseAmongAvailablePaths field.
     */
    if (transitionLogic.howToChooseAmongAvailablePaths === 'workgroupId') {
      return 'workgroup ID';
    } else if (transitionLogic.howToChooseAmongAvailablePaths === 'random') {
      return 'random assignment';
    }
  }

  /**
   * Get the previous node
   * @param nodeId get the node id that comes before this one
   * @return the node id that comes before
   */
  getPreviousNodeId(nodeId) {
    const flattenedNodeIds = this.getFlattenedProjectAsNodeIds();
    const indexOfNodeId = flattenedNodeIds.indexOf(nodeId);
    if (indexOfNodeId !== -1) {
      const indexOfPreviousNodeId = indexOfNodeId - 1;
      return flattenedNodeIds[indexOfPreviousNodeId];
    }
    return null;
  }

  setProjectScriptFilename(scriptFilename) {
    this.project.script = scriptFilename;
  }

  getProjectScriptFilename() {
    if (this.project != null && this.project.script != null) {
      return this.project.script;
    }
    return null;
  }

  /**
   * Check if a node has rubrics.
   * @param nodeId The node id of the node.
   * @return Whether the node has rubrics authored on it.
   */
  nodeHasRubric(nodeId) {
    return this.getNumberOfRubricsByNodeId(nodeId) > 0;
  }

  /**
   * Copy a component and insert it into the step
   * @param nodeId we are copying a component in this node
   * @param componentIds the components to copy
   * @param insertAfterComponentId Which component to place the new components
   * after. If this is null, we will put the new components at the beginning.
   * @return an array of the new components
   */
  copyComponentAndInsert(nodeId, componentIds, insertAfterComponentId) {
    const node = this.getNodeById(nodeId);
    const newComponents = [];
    const newComponentIds = [];
    for (const componentId of componentIds) {
      const newComponent = this.copyComponent(nodeId, componentId, newComponentIds);
      newComponents.push(newComponent);
      newComponentIds.push(newComponent.id);
    }

    let insertPosition = 0;
    if (insertAfterComponentId == null) {
      insertPosition = 0; // place the new components at the beginning
    } else {
      insertPosition =
        this.getComponentPositionByNodeIdAndComponentId(nodeId, insertAfterComponentId) + 1;
    }

    for (const newComponent of newComponents) {
      node.components.splice(insertPosition, 0, newComponent);
      insertPosition += 1;
    }
    return newComponents;
  }

  /**
   * Copy a component
   * @param nodeId the node id
   * @param componentId the compnent id
   * @param componentIdsToSkip component ids that we can't use for our new
   * component
   * @return a new component object
   */
  copyComponent(nodeId, componentId, componentIdsToSkip) {
    const component = this.getComponentByNodeIdAndComponentId(nodeId, componentId);
    const newComponent = this.UtilService.makeCopyOfJSONObject(component);
    newComponent.id = this.getUnusedComponentId(componentIdsToSkip);
    return newComponent;
  }

  /**
   * Import components from a project. Also import asset files that are
   * referenced in any of those components.
   * @param components an array of component objects that we are importing
   * @param importProjectId the id of the project we are importing from
   * @param nodeId the node we are adding the components to
   * @param insertAfterComponentId insert the components after this component id
   * @return an array of the new components
   */
  importComponents(components, importProjectId, nodeId, insertAfterComponentId) {
    const newComponents = [];
    const newComponentIds = [];
    for (const component of components) {
      const newComponent = this.UtilService.makeCopyOfJSONObject(component);
      let newComponentId = newComponent.id;
      if (this.isComponentIdUsed(newComponentId)) {
        newComponentId = this.getUnusedComponentId(newComponentIds);
        newComponent.id = newComponentId;
      }
      newComponents.push(newComponent);
      newComponentIds.push(newComponentId);
    }

    /*
     * Make the request to import the components. This will copy the asset files
     * and change file names if necessary. If an asset file with the same
     * name exists in both projects we will check if their content is the
     * same. If the content is the same we don't need to copy the file. If
     * the content is different, we need to make a copy of the file with a
     * new name and change all the references in the steps to use the new
     * name.
     */
    return this.http.post(this.ConfigService.getConfigParam('importStepsURL'),
      {
        steps: angular.toJson(newComponents),
        fromProjectId: importProjectId,
        toProjectId: this.ConfigService.getConfigParam('projectId')
      })
      .toPromise()
      .then((newComponents: any) => {
      const node = this.getNodeById(nodeId);
      let insertPosition = 0;
      if (insertAfterComponentId == null) {
        insertPosition = 0;
      } else {
        insertPosition =
          this.getComponentPositionByNodeIdAndComponentId(nodeId, insertAfterComponentId) + 1;
      }
      for (const newComponent of newComponents) {
        node.components.splice(insertPosition, 0, newComponent);
        insertPosition += 1;
      }
      return newComponents;
    });
  }

  /**
   * Delete a component from a node
   * @param nodeId the node id containing the node
   * @param componentId the component id
   */
  deleteComponent(nodeId, componentId) {
    const node = this.getNodeById(nodeId);
    const components = node.components;
    for (let c = 0; c < components.length; c++) {
      if (components[c].id === componentId) {
        components.splice(c, 1);
        break;
      }
    }
  }

  deleteTransition(node, transition) {
    const nodeTransitions = node.transitionLogic.transitions;
    const index = nodeTransitions.indexOf(transition);
    if (index > -1) {
      nodeTransitions.splice(index, 1);
    }
    if (nodeTransitions.length <= 1) {
      // these settings only apply when there are multiple transitions
      node.transitionLogic.howToChooseAmongAvailablePaths = null;
      node.transitionLogic.whenToChoosePath = null;
      node.transitionLogic.canChangePath = null;
      node.transitionLogic.maxPathsVisitable = null;
    }
  }

  /**
   * Get the branch path letter
   * @param nodeId get the branch path letter for this node if it is in a branch
   * @return the branch path letter for the node if it is in a branch
   */
  getBranchPathLetter(nodeId) {
    return this.nodeIdToBranchPathLetter[nodeId];
  }

  /**
   * Set the node into the project by replacing the existing node with the
   * given node id
   * @param nodeId the node id of the node
   * @param node the node object
   */
  setNode(nodeId, node) {
    for (let n = 0; n < this.project.nodes.length; n++) {
      const tempNode = this.project.nodes[n];
      if (tempNode.id == nodeId) {
        this.project.nodes[n] = node;
      }
    }
    for (let i = 0; i < this.project.inactiveNodes.length; i++) {
      const tempNode = this.project.inactiveNodes[i];
      if (tempNode.id == nodeId) {
        this.project.inactiveNodes[i] = node;
      }
    }
    this.idToNode[nodeId] = node;
  }

  getIdToNode() {
    return this.idToNode;
  }

  turnOnSaveButtonForAllComponents(node) {
    for (const component of node.components) {
      const service = this.upgrade.$injector.get(component.type + 'Service');
      if (service.componentUsesSaveButton()) {
        component.showSaveButton = true;
      }
    }
  }

  turnOffSaveButtonForAllComponents(node) {
    for (const component of node.components) {
      const service = this.upgrade.$injector.get(component.type + 'Service');
      if (service.componentUsesSaveButton()) {
        component.showSaveButton = false;
      }
    }
  }

  checkPotentialStartNodeIdChangeThenSaveProject() {
    this.checkPotentialStartNodeIdChange();
    return this.saveProject();
  }

  checkPotentialStartNodeIdChange() {
    const firstLeafNodeId = this.getFirstLeafNodeId();
    if (firstLeafNodeId == null) {
      this.setStartNodeId('');
    } else {
      const currentStartNodeId = this.getStartNodeId();
      if (currentStartNodeId != firstLeafNodeId) {
        this.setStartNodeId(firstLeafNodeId);
      }
    }
  }

  /**
   * Remove the node from the active nodes.
   * If the node is a group node, also remove its children.
   * @param nodeId the node to remove
   * @returns the node that was removed
   */
  removeNodeFromActiveNodes(nodeId) {
    let nodeRemoved = null;
    const activeNodes = this.project.nodes;
    for (let a = 0; a < activeNodes.length; a++) {
      const activeNode = activeNodes[a];
      if (activeNode.id === nodeId) {
        activeNodes.splice(a, 1);
        nodeRemoved = activeNode;
        if (activeNode.type === 'group') {
          this.removeChildNodesFromActiveNodes(activeNode);
        }
        break;
      }
    }
    return nodeRemoved;
  }

  /**
   * Move the child nodes of a group from the active nodes.
   * @param node The group node.
   */
  removeChildNodesFromActiveNodes(node) {
    for (const childId of node.ids) {
      this.removeNodeFromActiveNodes(childId);
    }
  }

  /**
   * Move an active node to the inactive nodes array.
   * @param node the node to move
   * @param nodeIdToInsertAfter place the node after this
   */
  moveToInactive(node, nodeIdToInsertAfter) {
    if (this.isActive(node.id)) {
      this.removeNodeFromActiveNodes(node.id);
      this.addInactiveNodeInsertAfter(node, nodeIdToInsertAfter);
    }
  }

  /**
   * Add the node to the inactive nodes array.
   * @param node the node to move
   * @param nodeIdToInsertAfter place the node after this
   */
  addInactiveNodeInsertAfter(node, nodeIdToInsertAfter = null) {
    this.clearTransitionsFromNode(node);
    if (this.isNodeIdToInsertTargetNotSpecified(nodeIdToInsertAfter)) {
      this.insertNodeAtBeginningOfInactiveNodes(node);
    } else {
      this.insertNodeAfterInactiveNode(node, nodeIdToInsertAfter);
    }
    if (node.type === 'group') {
      this.inactiveGroupNodes.push(node.id);
      this.addGroupChildNodesToInactive(node);
    } else {
      this.inactiveStepNodes.push(node.id);
    }
  }

  clearTransitionsFromNode(node) {
    if (node.transitionLogic != null) {
      node.transitionLogic.transitions = [];
    }
  }

  insertNodeAtBeginningOfInactiveNodes(node) {
    this.project.inactiveNodes.splice(0, 0, node);
  }

  insertNodeAfterInactiveNode(node, nodeIdToInsertAfter) {
    const inactiveNodes = this.getInactiveNodes();
    for (let i = 0; i < inactiveNodes.length; i++) {
      if (inactiveNodes[i].id === nodeIdToInsertAfter) {
        const parentGroup = this.getParentGroup(nodeIdToInsertAfter);
        if (parentGroup != null) {
          this.insertNodeAfterInGroups(node.id, nodeIdToInsertAfter);
          this.insertNodeAfterInTransitions(node, nodeIdToInsertAfter);
        }
        inactiveNodes.splice(i + 1, 0, node);
      }
    }
  }

  isNodeIdToInsertTargetNotSpecified(nodeIdToInsertTarget) {
    return [null, 'inactiveNodes', 'inactiveSteps', 'inactiveGroups'].includes(
      nodeIdToInsertTarget
    );
  }

  /**
   * Move the node from active to inside an inactive group
   * @param node the node to move
   * @param nodeIdToInsertInside place the node inside this
   */
  moveFromActiveToInactiveInsertInside(node, nodeIdToInsertInside) {
    this.removeNodeFromActiveNodes(node.id);
    this.addInactiveNodeInsertInside(node, nodeIdToInsertInside);
  }

  /**
   * Move the node from inactive to inside an inactive group
   * @param node the node to move
   * @param nodeIdToInsertInside place the node inside this
   */
  moveFromInactiveToInactiveInsertInside(node, nodeIdToInsertInside) {
    this.removeNodeFromInactiveNodes(node.id);
    if (this.isGroupNode(node.id)) {
      /*
       * remove the group's child nodes from our data structures so that we can
       * add them back in later
       */
      for (const childId of node.ids) {
        const childNode = this.getNodeById(childId);
        const inactiveNodesIndex = this.project.inactiveNodes.indexOf(childNode);
        if (inactiveNodesIndex != -1) {
          this.project.inactiveNodes.splice(inactiveNodesIndex, 1);
        }
        const inactiveStepNodesIndex = this.inactiveStepNodes.indexOf(childNode);
        if (inactiveStepNodesIndex != -1) {
          this.inactiveStepNodes.splice(inactiveStepNodesIndex, 1);
        }
      }
    }
    this.addInactiveNodeInsertInside(node, nodeIdToInsertInside);
  }

  addInactiveNodeInsertInside(node, nodeIdToInsertInside = null) {
    this.clearTransitionsFromNode(node);
    if (this.isNodeIdToInsertTargetNotSpecified(nodeIdToInsertInside)) {
      this.insertNodeAtBeginningOfInactiveNodes(node);
    } else {
      this.insertNodeInsideInactiveNode(node, nodeIdToInsertInside);
    }
    if (node.type === 'group') {
      this.inactiveGroupNodes.push(node.id);
      this.addGroupChildNodesToInactive(node);
    } else {
      this.inactiveStepNodes.push(node.id);
    }
  }

  insertNodeInsideInactiveNode(node, nodeIdToInsertInside) {
    const inactiveNodes = this.getInactiveNodes();
    const inactiveGroupNodes = this.getInactiveGroupNodes();
    for (const inactiveGroup of inactiveGroupNodes) {
      if (nodeIdToInsertInside === inactiveGroup.id) {
        this.insertNodeInsideOnlyUpdateTransitions(node.id, nodeIdToInsertInside);
        this.insertNodeInsideInGroups(node.id, nodeIdToInsertInside);
        for (let i = 0; i < inactiveNodes.length; i++) {
          if (inactiveNodes[i].id === nodeIdToInsertInside) {
            inactiveNodes.splice(i + 1, 0, node);
          }
        }
      }
    }
  }

  moveInactiveNodeToInactiveSection(node, nodeIdToInsertAfter) {
    this.removeNodeFromInactiveNodes(node.id);
    this.addInactiveNodeInsertAfter(node, nodeIdToInsertAfter);
  }

  addNodeToGroup(node, group) {
    if (this.isGroupHasNode(group)) {
      this.insertAfterLastNode(node, group);
    } else {
      this.insertAsFirstNode(node, group);
    }
  }

  isGroupHasNode(group) {
    return group.ids.length != 0;
  }

  getLastNodeInGroup(group) {
    const lastNodeId = group.ids[group.ids.length - 1];
    return this.idToNode[lastNodeId];
  }

  insertAsFirstNode(node, group) {
    this.insertNodeInsideOnlyUpdateTransitions(node.id, group.id);
    this.insertNodeInsideInGroups(node.id, group.id);
  }

  insertAfterLastNode(node, group) {
    const lastNode = this.getLastNodeInGroup(group);
    this.insertNodeAfterInTransitions(node, lastNode.id);
    this.insertNodeAfterInGroups(node.id, lastNode.id);
  }

  createNodeAndAddToLocalStorage(nodeTitle) {
    const node = this.createNode(nodeTitle);
    this.setIdToNode(node.id, node);
    this.addNode(node);
    this.applicationNodes.push(node);
    return node;
  }

  getLibraryProjects() {
    return this.http
      .get(this.ConfigService.getConfigParam('getLibraryProjectsURL'))
      .toPromise()
      .then(projects => {
        return projects;
      });
  }

  sortAndFilterUniqueLibraryProjects(libraryProjects) {
    const flatProjectList = libraryProjects
      .map(grade => {
        return grade.children;
      })
      .flat();
    return this.filterUniqueProjects(flatProjectList).sort(this.sortByProjectIdDescending);
  }

  filterUniqueProjects(projects) {
    const uniqueProjects = [];
    const filteredProjects = {};
    for (const project of projects) {
      if (filteredProjects[project.id] == null) {
        filteredProjects[project.id] = project;
        uniqueProjects.push(project);
      }
    }
    return uniqueProjects;
  }

  sortByProjectIdDescending(project1, project2) {
    if (project1.id > project2.id) {
      return -1;
    } else {
      return 1;
    }
  }

  getAutomatedAssessmentProjectId(): number {
    return this.ConfigService.getConfigParam('automatedAssessmentProjectId') || -1;
  }

  getNodeIdsAndComponentIds(nodeId) {
    const nodeIdAndComponentIds = [];
    const node = this.getNodeById(nodeId);
    for (const component of node.components) {
      const nodeIdAndComponentId = {
        nodeId: nodeId,
        componentId: component.id
      };
      nodeIdAndComponentIds.push(nodeIdAndComponentId);
    }
    return nodeIdAndComponentIds;
  }

  /**
   * Get the branch letter in the node position string if the node is in a branch path
   * @param nodeId the node id we want the branch letter for
   * @return the branch letter in the node position if the node is in a branch path
   */
  getBranchLetter(nodeId) {
    const nodePosition = this.getNodePositionById(nodeId);
    const branchLetterRegex = /.*([A-Z])/;
    const match = branchLetterRegex.exec(nodePosition);
    if (match != null) {
      return match[1];
    }
    return null;
  }

  nodeChanged(doParseProject: boolean = false): void {
    this.nodeChangedSource.next(doParseProject);
  }

  refreshProject() {
    this.refreshProjectSource.next();
  }

  scrollToBottomOfPage() {
    this.scrollToBottomOfPageSource.next();
  }

  showAdvancedComponentView(componentId: string, isShow: boolean) {
    this.showAdvancedComponentViewSource
        .next({componentId: componentId, isShow: isShow});
  }

  addTeacherRemovalConstraint(node: any, periodId: number) {
    const lockConstraint = {
      id: this.UtilService.generateKey(),
      action: 'makeThisNodeNotVisitable',
      targetId: node.id,
      removalConditional: 'any',
      removalCriteria: [{
        'name': 'teacherRemoval',
        'params': {
          periodId: periodId
        }
      }]
    };
    this.addConstraintToNode(node, lockConstraint);
  }

  removeTeacherRemovalConstraint(node: any, periodId: number) {
    node.constraints = node.constraints.filter(constraint => {
      return !(constraint.action === 'makeThisNodeNotVisitable' &&
          constraint.targetId === node.id &&
          constraint.removalCriteria[0].name === 'teacherRemoval' &&
          constraint.removalCriteria[0].params.periodId === periodId);
    });
  }

  openWISELinkChooser({ projectId, nodeId, componentId, target }): any {
    const stateParams = {
      projectId: projectId,
      nodeId: nodeId,
      componentId: componentId,
      target: target
    };
    return this.upgrade.$injector.get('$mdDialog').show({
      templateUrl: 'wise5/authoringTool/wiseLink/wiseLinkAuthoring.html',
      controller: 'WISELinkAuthoringController',
      controllerAs: 'wiseLinkAuthoringController',
      $stateParams: stateParams,
      clickOutsideToClose: true,
      escapeToClose: true
    });
  }

}
