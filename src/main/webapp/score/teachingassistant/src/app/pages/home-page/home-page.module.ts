import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {HomePageRoutingModule} from './home-page-routing.module';
import {HomePageComponent} from './home-page.component';
import {MatTableModule} from "@angular/material/table";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {MatButtonModule} from "@angular/material/button";
import {MatPaginatorModule} from "@angular/material/paginator";

@NgModule({
  declarations: [HomePageComponent],
    imports: [
        CommonModule,
        HomePageRoutingModule,
        MatTableModule,
        MatButtonModule,
        MatFormFieldModule,
        MatSelectModule,
        MatPaginatorModule
    ]
})
export class HomePageModule { }
