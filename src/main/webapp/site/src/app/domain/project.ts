import { Run } from './run';
import { User } from '../domain/user';

export class Project {
  id: number;
  name: string;
  metadata: any;
  dateCreated: string;
  dateArchived: string;
  lastEdited: string;
  projectThumb: string;
  thumbStyle: any;
  isHighlighted: boolean;
  owner: User;
  sharedOwners: User[] = [];
  run: Run;
  parentId: number;
  wiseVersion: number;
  uri: String;
  license: String;
  content: any;
  nodes: any[] = [];
  idToNode: any = {};
  idToOrder: any = {};

  static readonly VIEW_PERMISSION: number = 1;
  static readonly EDIT_PERMISSION: number = 2;

  constructor(jsonObject: any = {}) {
    for (const key of Object.keys(jsonObject)) {
      const value = jsonObject[key];
      if (key === 'owner') {
        this[key] = new User(value);
      } else if (key === 'sharedOwners') {
        const sharedOwners: User[] = [];
        for (const sharedOwner of value) {
          sharedOwners.push(new User(sharedOwner));
        }
        this[key] = sharedOwners;
      } else if (key === 'metadata') {
        this[key] = this.parseMetadata(value);
      } else {
        this[key] = value;
      }
    }
  }

  public canView(userId) {
    return (
      this.isOwner(userId) || this.isSharedOwnerWithPermission(userId, Project.VIEW_PERMISSION)
    );
  }

  public canEdit(userId) {
    return (
      this.isOwner(userId) || this.isSharedOwnerWithPermission(userId, Project.EDIT_PERMISSION)
    );
  }

  public isChild() {
    return this.parentId != null;
  }

  isOwner(userId) {
    return this.owner.id === userId;
  }

  isSharedOwnerWithPermission(userId, permissionId) {
    for (const sharedOwner of this.sharedOwners) {
      if (sharedOwner.id === userId) {
        return this.userHasPermission(sharedOwner, permissionId);
      }
    }
    return false;
  }

  userHasPermission(user: User, permission: number) {
    return user.permissions.includes(permission);
  }

  parseMetadata(metadata) {
    if (typeof metadata.authors === 'string') {
      metadata.authors = JSON.parse(metadata.authors);
    }
    if (typeof metadata.grades === 'string') {
      metadata.grades = JSON.parse(metadata.grades);
    }
    if (typeof metadata.parentProjects === 'string') {
      metadata.parentProjects = JSON.parse(metadata.parentProjects);
    }
    if (typeof metadata.standardsAddressed === 'string') {
      metadata.standardsAddressed = JSON.parse(metadata.standardsAddressed);
    }
    return metadata;
  }

  public setContent(content) {
    this.content = content;
    this.initIdToNode();
    this.idToOrder = this.getNodeOrderOfProject();
  }

  initIdToNode() {
    for (const node of this.content.nodes) {
      this.idToNode[node.id] = node;
    }
  }

  getNodeOrderOfProject() {
    const rootNode = this.getNodeById(this.content.startGroupId, this.content);
    const idToOrder = {
      nodeCount: 0
    };
    const stepNumber = '';
    const nodes = [];
    const projectIdToOrder = this.getNodeOrderOfProjectHelper(
      this.content,
      rootNode,
      idToOrder,
      stepNumber,
      nodes
    );
    delete projectIdToOrder.nodeCount;
    return {
      idToOrder: projectIdToOrder,
      nodes: nodes
    };
  }

  getNodeOrderOfProjectHelper(project, node, idToOrder, stepNumber, nodes) {
    const item = {
      order: idToOrder.nodeCount,
      node: node,
      stepNumber: stepNumber
    };

    idToOrder[node.id] = item;
    idToOrder.nodeCount++;
    nodes.push(item);

    if (node.type === 'group') {
      const childIds = node.ids;
      for (let c = 0; c < childIds.length; c++) {
        const childId = childIds[c];
        const child = this.getNodeById(childId, project);
        let childStepNumber = stepNumber;
        if (childStepNumber != '') {
          childStepNumber += '.';
        }
        childStepNumber += c + 1;
        this.getNodeOrderOfProjectHelper(project, child, idToOrder, childStepNumber, nodes);
      }
    }
    return idToOrder;
  }

  getNodeById(nodeId, project = this.content) {
    for (const node of project.nodes.concat(project.inactiveNodes)) {
      if (node.id === nodeId) {
        return node;
      }
    }
    return null;
  }
}
