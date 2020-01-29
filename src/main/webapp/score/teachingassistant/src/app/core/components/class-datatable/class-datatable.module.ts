import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ClassDatatableComponent} from './class-datatable.component';
import {MatTableModule} from '@angular/material';

@NgModule({
  declarations: [ClassDatatableComponent],
    exports: [ClassDatatableComponent],
    imports: [CommonModule, MatTableModule]
})
export class ClassDatatableModule { }
