import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {LogoComponent} from '../logo/logo.component';
import {RunDatatableComponent} from "./run-datatable.component";
import {MatTableModule} from "@angular/material/table";

@NgModule({
    declarations: [RunDatatableComponent],
    exports: [RunDatatableComponent],
    imports: [CommonModule, MatTableModule],
})
export class RunDatatableComponentModule {}
