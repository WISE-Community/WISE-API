import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {HomePageRoutingModule} from './home-page-routing.module';
import {HomePageComponent} from './home-page.component';
import {ClassDatatableModule} from '../../core/components/class-datatable/class-datatable.module';

@NgModule({
  declarations: [HomePageComponent],
    imports: [
        CommonModule,
        HomePageRoutingModule,
        ClassDatatableModule
    ]
})
export class HomePageModule { }
