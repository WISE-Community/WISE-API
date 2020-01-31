import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {ClassesPageRoutingModule} from './classes-page-routing.module';
import {ClassesPageComponent} from './classes-page.component';
import {ClassDatatableModule} from '../../core/components/class-datatable/class-datatable.module';

@NgModule({
  declarations: [ClassesPageComponent],
  imports: [
    CommonModule,
    ClassesPageRoutingModule,
      ClassDatatableModule
  ]
})
export class ClassesPageModule { }
