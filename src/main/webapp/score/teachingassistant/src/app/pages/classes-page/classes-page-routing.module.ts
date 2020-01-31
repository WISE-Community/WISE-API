import {ClassesPageComponent} from './classes-page.component';

import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

const routes: Routes = [
    {
        path: '',
        component: ClassesPageComponent,
        data: {shouldReuse: true, key: 'classes'}
    },

];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class ClassesPageRoutingModule {
}
