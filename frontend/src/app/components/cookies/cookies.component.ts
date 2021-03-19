import { Component } from '@angular/core';

@Component({
  selector: 'app-cookies',
  templateUrl: './cookies.component.html',
  styleUrls: ['./cookies.component.scss']
})
export class CookiesComponent {

  private _cookiesKey: string = 'cookiesClosed';
  public show: boolean = localStorage.getItem(this._cookiesKey) === null;

  public close(): void {
    localStorage.setItem(this._cookiesKey, 'true');
    this.show = false;
  }
}
