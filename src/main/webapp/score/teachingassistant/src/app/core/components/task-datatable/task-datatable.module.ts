import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatTableModule} from "@angular/material/table";
import {TaskDatatableComponent} from "./task-datatable.component";
import {MatButtonModule} from "@angular/material/button";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatSortModule} from "@angular/material/sort";

@NgModule({
    declarations: [TaskDatatableComponent],
    exports: [TaskDatatableComponent],
    imports: [CommonModule,
        MatTableModule,
        MatButtonModule,
        MatFormFieldModule,
        MatSelectModule,
        MatSortModule,
        MatPaginatorModule,],
})
export class TaskDatatableComponentModule {
}
