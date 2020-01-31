import {NgModule} from '@angular/core';
import {PreloadAllModules, RouteReuseStrategy, RouterModule, Routes,} from '@angular/router';
import {navRoutes, sideNavPath} from './nav-routing';
import {NavComponent} from './core/components/nav/nav.component';
// import { AuthGuard } from './auth/auth.guard';
import {CustomRouteReuseStrategy} from './core/nav-reuse-strategy';
import {NavGuard} from './core/nav.guard';

const routes: Routes = [{path: 'instructor',loadChildren: () =>
                import('./pages/instructor-page/instructor-page.module').then(
                    m => m.InstructorPageModule,
                ),},{path: 'classes',loadChildren: () =>
                import('./pages/classes-page/classes-page.module').then(
                    m => m.ClassesPageModule,
                ),},
    {
        path: sideNavPath,
        component: NavComponent,
        children: navRoutes,
        // canActivate: [AuthGuard],
        canActivateChild: [NavGuard],
    },
    {
        path: '**',
        redirectTo: sideNavPath,
    },
];

@NgModule({
    imports: [
        RouterModule.forRoot(routes, { preloadingStrategy: PreloadAllModules }),
    ],
    exports: [RouterModule],
    providers: [
        { provide: RouteReuseStrategy, useClass: CustomRouteReuseStrategy },
    ],
})
export class AppRoutingModule {}
