import {Injectable} from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest,} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable()
export class HttpConfigInterceptor implements HttpInterceptor {
    intercept(
        req: HttpRequest<any>,
        next: HttpHandler,
    ): Observable<HttpEvent<any>> {
        return null;
        // throw new Error('Method not implemented.');
    }
    constructor() {}

    // intercept(
    //     req: HttpRequest<any>,
    //     next: HttpHandler,
    // ): Observable<HttpEvent<any>> {
    //     const token: string = this.authService.getToken();
    //
    //     if (token) {
    //         req = req.clone({
    //             setHeaders: { Authorization: `Bearer ${token}` },
    //         });
    //     }
    //
    //     return next.handle(req);
    // }
}
