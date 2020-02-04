import {NgModule} from '@angular/core';
import {PreloadAllModules, RouteReuseStrategy, RouterModule, Routes,} from '@angular/router';
import {navRoutes, sideNavPath} from './nav-routing';
import {NavComponent} from './core/components/nav/nav.component';
import {CustomRouteReuseStrategy} from './core/nav-reuse-strategy';
import {NavGuard} from './core/nav.guard';
import {AuthGuard} from "./auth/auth.guard";

const routes: Routes = [
    {
        path: 'home', loadChildren: () =>
            import('./pages/home-page/home-page.module').then(
                m => m.HomePageModule,
            ),
    },
    {
        path: 'instructor', loadChildren: () =>
            import('./pages/instructor-page/instructor-page.module').then(
                m => m.InstructorPageModule,
            ),
    }, {
        path: 'classes', loadChildren: () =>
            import('./pages/classes-page/classes-page.module').then(
                m => m.ClassesPageModule,
            ),
    },
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
    {
        path: '',
        redirectTo: sideNavPath,
        pathMatch: 'full'
    },
];

@NgModule({
    imports: [
        RouterModule.forRoot(routes, {preloadingStrategy: PreloadAllModules, enableTracing: false}),
    ],
    exports: [RouterModule],
    providers: [
        {provide: RouteReuseStrategy, useClass: CustomRouteReuseStrategy},
    ],
})
export class AppRoutingModule {
}
