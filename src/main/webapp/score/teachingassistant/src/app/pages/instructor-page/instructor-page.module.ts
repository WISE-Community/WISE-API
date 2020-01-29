import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {InstructorPageRoutingModule} from './instructor-page-routing.module';
import {InstructorPageComponent} from './instructor-page.component';

@NgModule({
  declarations: [InstructorPageComponent],
  imports: [
    CommonModule,
    InstructorPageRoutingModule
  ]
})
export class InstructorPageModule { }
