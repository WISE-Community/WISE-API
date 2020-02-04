import { Injectable } from '@angular/core';
import {
    ActivatedRouteSnapshot,
    RouterStateSnapshot,
    UrlTree,
    CanActivate,
    Router,
} from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({
    providedIn: 'root',
})
export class AuthGuard implements CanActivate {
    constructor(private authService: AuthService, private router: Router) {}

    canActivate(
        route: ActivatedRouteSnapshot,
        state: RouterStateSnapshot,
    ):
        | Observable<boolean | UrlTree>
        | Promise<boolean | UrlTree>
        | boolean
        | UrlTree {
        if (this.authService.isLogged()) {
            this.authService.redirectUrl = null;
            return true;
        }
        // this.authService.redirectUrl = state.url;
        // console.log('---> ', state.url, window.location.href)
        // // this.authService.redirectUrl = '/nav/home'
        // // this.router.navigate(['/nav'], { queryParams: { runId: 4 } });
        // // this.router.navigate(['/'] );
        return true;
    }
}
