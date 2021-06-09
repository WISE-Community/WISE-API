import { Directive, Input } from '@angular/core';
import { ProjectService } from '../../../services/projectService';

@Directive()
export abstract class ComponentGrading {
  @Input()
  nodeId: string;

  @Input()
  componentId: string;

  @Input()
  componentState: any;

  @Input()
  isRevision: any = false;

  componentContent: any;

  constructor(protected ProjectService: ProjectService) {}

  ngOnInit() {
    this.componentState = JSON.parse(this.componentState);
    this.componentContent = this.ProjectService.getComponentByNodeIdAndComponentId(
      this.nodeId,
      this.componentId
    );
    this.isRevision = this.isRevision === 'true';
  }
}
