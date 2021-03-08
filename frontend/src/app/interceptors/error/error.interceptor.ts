import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '@app/services/auth/auth.service';
import { catchError } from 'rxjs/operators';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {

  constructor(private _authService: AuthService) {
  }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(catchError(err => {

      if (err.status === 401) {
        // auto logout if 401 response returned from api
        this._authService.logout();
        throw new Error('Špatné přihlašovací údaje');
      }

      const message = err.name || err.message;
      throw new Error(message);
    }));
  }
}
