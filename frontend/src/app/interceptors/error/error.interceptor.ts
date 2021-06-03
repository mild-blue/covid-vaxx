import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '@app/services/auth/auth.service';
import { catchError } from 'rxjs/operators';
import { PatientRegistrationDtoIn } from '@app/generated';
import { environment } from '@environments/environment';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {

  constructor(private _authService: AuthService) {
  }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const companyEmail = environment.companyEmail;

    return next.handle(request).pipe(catchError(err => {

      const error = err.error;
      const requestId = error?.requestId;

      const defaultMessage = 'Něco se pokazilo. Zkuste to prosím znovu.';
      const defaultSubject = 'Problém na webu';

      let message = defaultMessage;
      let contactMessage = 'V případě potíží nás kontaktujte na';
      let emailSubject = defaultSubject;

      if (err.status === 401) {
        // auto logout if 401 response returned from api
        this._authService.logout();
        message = 'Špatné přihlašovací údaje. Zkuste to prosím znovu.';
      } else if (err.status === 403) {
        message = 'Přístup byl odmítnut.';
        emailSubject = 'Požadavek na udělení přístupu';
      } else if (err.status === 404) {
        // TODO Pres href a lepe. Mame na to uz button
        message = 'Je nám líto, ale Vašemu dotazu nic neodpovídá. Zkuste pacienta registrovat na ockovani.mild.blue.';
      } else if (err.status === 409) {

        message = 'Myslíme si, že pacient s Vašimi údaji je již registrován.';
        contactMessage = 'Pokud potřebujete opravit svoji registraci, napište nám na';

        emailSubject = 'Problém s registaci nového pacienta';
        const personalNumber = (request.body as PatientRegistrationDtoIn).personalNumber;
        if (personalNumber) {
          emailSubject += ` s rodným číslem ${personalNumber}`;
        }
      } else if (err.status === 429) {
        message = 'Server je zahlcen požadavky, zkuste to prosím za hodinu znovu.';
      } else if (err.status === 0) {
        message = 'Zkontrolujte prosím své připojení k internetu a znovu načtěte stránku.';
      }

      emailSubject += requestId ? ` (ID požadavku: ${requestId})` : '';
      message += ` ${contactMessage} <a href="mailto:${companyEmail}?subject=${emailSubject}">${companyEmail}</a>.`;
      message += requestId ? ` Při komunikaci uveďte ID požadavku: <b>${requestId}</b>.` : '';

      throw new Error(message);
    }));
  }
}
