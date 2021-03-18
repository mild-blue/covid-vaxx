import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '@app/services/auth/auth.service';
import { catchError } from 'rxjs/operators';
import { PatientRegistrationDtoIn } from '@app/generated';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {

  constructor(private _authService: AuthService) {
  }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(catchError(err => {
      if (err.status === 401) {
        // auto logout if 401 response returned from api
        this._authService.logout();
        throw new Error('Uživatel nenalezen. Zkontrolujte prosím správnost přihlašovacích údajů.');
      } else if (err.status === 404) {
        throw new Error('Je nám líto, ale Vašemu dotazu nic neodpovídá.');
      } else if (err.status === 409) {
        const personalNumber = (request.body as PatientRegistrationDtoIn).personalNumber;
        let subject = 'Problém s registaci nového pacienta';
        if (personalNumber) {
          subject += ` s rodným číslem ${personalNumber}`;
        }
        throw new Error(`Myslíme si, že jste se už pokusil jednou registrovat. Pokud potřebujete opravit svoji registraci, napište nám na <a href="mailto:ockovani@mild.blue?subject=${subject}">ockovani@mild.blue</a>`);
      } else if (err.status === 0) {
        throw new Error('Zkontrolujte prosím své připojení k internetu a znovu načtěte stránku.');
      }

      const error = err.error;
      const message = error?.message ? error.message : 'Něco se pokazilo. Zkuste to prosím znovu.';
      throw new Error(message);
    }));
  }
}
