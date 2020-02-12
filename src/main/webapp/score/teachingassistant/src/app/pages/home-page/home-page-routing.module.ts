import {HomePageComponent} from './home-page.component';

import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

const routes: Routes = [ {path:'',component:HomePageComponent,data:{shouldReuse:true,key:'Classes'}},  ];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class HomePageRoutingModule { }
