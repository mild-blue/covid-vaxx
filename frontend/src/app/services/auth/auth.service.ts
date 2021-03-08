import { Injectable } from '@angular/core';
import { AuthResponse } from '@app/services/auth/auth.interface';
import { map } from 'rxjs/operators';
import { environment } from '@environments/environment';
import { BehaviorSubject, Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import * as jwt_decode from 'jwt-decode';
import { DecodedToken, User } from '@app/model/User';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private _currentUserSubject: BehaviorSubject<User | undefined> = new BehaviorSubject<User | undefined>(undefined);
  public currentUser: Observable<User | undefined> = this._currentUserSubject.asObservable();

  constructor(private _http: HttpClient,
              private _router: Router) {
    this._setCurrentUser();
  }

  get currentUserValue(): User | undefined {
    return this._currentUserSubject.value;
  }

  get isLoggedIn(): boolean {
    const user = this.currentUserValue;
    const token = user?.decoded;

    if (!token) {
      return false;
    }

    // check if token is valid
    return token.exp > (Date.now() / 1000);
  }

  public login(username: string, password: string): Observable<User> {
    return this._http.post(
      `${environment.apiUrl}/login`,
      { username, password }
    ).pipe(
      map((r: Object) => {
        const response = r as AuthResponse;
        const token = response.token;
        const decoded = jwt_decode(token) as DecodedToken;
        const user: User = { username, token, decoded };

        localStorage.setItem('user', JSON.stringify(user));
        this._currentUserSubject.next(user);

        return user;
      })
    );
  }

  public logout(): void {
    localStorage.removeItem('user');
    this._currentUserSubject.next(undefined);
    this._router.navigate(['/login']);
  }

  private _setCurrentUser(): void {
    const lsUser = localStorage.getItem('user');
    if (lsUser) {
      this._currentUserSubject.next(JSON.parse(lsUser));
      this.currentUser = this._currentUserSubject.asObservable();
    }
  }
}
