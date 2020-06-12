
import {Component, NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';
import { CommonModule } from '@angular/common';

import teacher from '../../../../wise5/teacher/teacher';
import {UpgradeModule} from '@angular/upgrade/static';
import {setUpLocationSync} from '@angular/router/upgrade';
import { MatIconModule, MatTableModule, MatFormFieldModule, MatPaginatorModule, MatSelectModule } from '@angular/material';
import { TaskDatatableComponent } from '../../../../score/teachingassistant/src/app/core/components/task-datatable/task-datatable.component';

@Component({template: ``})
export class EmptyComponent {}

@NgModule({
  declarations: [
    EmptyComponent,
    TaskDatatableComponent
  ],
  imports: [
    CommonModule,
    MatIconModule,
    MatTableModule,
    MatFormFieldModule,
    MatPaginatorModule,
    MatSelectModule,
    UpgradeModule,
    RouterModule.forChild([
      {path: '**', component: EmptyComponent}
    ])
  ],
  entryComponents: [
    TaskDatatableComponent
  ]
})
export class TeacherAngularJSModule {
  // The constructor is called only once, so we bootstrap the application
  // only once, when we first navigate to the legacy part of the app.
  constructor(upgrade: UpgradeModule) {
    upgrade.bootstrap(document.body, [teacher.name]);
    setUpLocationSync(upgrade);
  }
}
