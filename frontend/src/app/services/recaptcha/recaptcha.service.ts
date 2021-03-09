import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map } from 'rxjs/operators';
import { AuthResponse } from '@app/services/auth/auth.interface';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';

@Injectable({
  providedIn: 'root'
})
export class RecaptchaService {

  constructor(private _http: HttpClient) {
  }

  public verifyUserWithRecaptcha(): void {
    if (!grecaptcha) {
      console.error('Recaptcha does not exist');
    }

    grecaptcha.ready(() => {
      grecaptcha.execute(environment.recaptchaSiteKey, { action: 'submit' }).then((token: string) => {
        console.log('Received recaptcha token');
        this._validateToken(token);
      });
    });
  }

  private _validateToken(token: string): Observable<string> {
    console.log('Sending token to BE', token, `${environment.apiUrl}/captcha`);
    return this._http.post<string>(
      `${environment.apiUrl}/captcha`,
      { token: 'prdel' }
    ).pipe(
      map((r: Object) => {
        console.log('Received BE token', r);
        const response = r as AuthResponse;
        const token = response.token;

        localStorage.setItem('token', token);
        return token;
      })
    );
  }

}
