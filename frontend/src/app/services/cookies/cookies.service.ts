import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class CookiesService {

  private _cookiesKey: string = 'cookiesClosed';

  constructor() {
  }

  public shouldShowBanner(): boolean {
    return localStorage.getItem(this._cookiesKey) === undefined;
  }

  public closeCookieBanner(): void {
    localStorage.setItem(this._cookiesKey, 'true');
  }
}
