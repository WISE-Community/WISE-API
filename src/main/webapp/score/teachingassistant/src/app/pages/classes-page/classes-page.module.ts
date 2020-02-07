import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {ClassesPageRoutingModule} from './classes-page-routing.module';
import {ClassesPageComponent} from './classes-page.component';

@NgModule({
  declarations: [ClassesPageComponent],
  imports: [
    CommonModule,
    ClassesPageRoutingModule,
  ]
})
export class ClassesPageModule { }
