import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {HomePageRoutingModule} from './home-page-routing.module';
import {HomePageComponent} from './home-page.component';
import {MatTableModule} from "@angular/material/table";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {MatButtonModule} from "@angular/material/button";
import {MatPaginatorModule} from "@angular/material/paginator";
import {RunDatatableComponent} from "../../core/components/run-datatable/run-datatable.component";
import {RunDatatableComponentModule} from "../../core/components/run-datatable/run-datatable.module";
import {TaskDatatableComponentModule} from "../../core/components/task-datatable/task-datatable.module";

@NgModule({
  declarations: [HomePageComponent],
    imports: [
        CommonModule,
        MatTableModule,
        HomePageRoutingModule,
        RunDatatableComponentModule,
        TaskDatatableComponentModule
    ]
})
export class HomePageModule { }
