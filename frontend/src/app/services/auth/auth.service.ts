import { Injectable } from '@angular/core';
import { first, map } from 'rxjs/operators';
import { environment } from '@environments/environment';
import { BehaviorSubject, Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import * as jwt_decode from 'jwt-decode';
import { DecodedToken, User, UserRole } from '@app/model/User';
import { CredentialsDtoIn, LoginDtoIn, PersonnelDtoOut, UserLoginResponseDtoOut } from '@app/generated';
import { parseNurse } from '@app/parsers/nurse.parser';
import { Nurse } from '@app/model/Nurse';

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

  public getNurses(email: string, password: string): Observable<Nurse[]> {

    const body: CredentialsDtoIn = {
      email,
      password
    };

    return this._http.post<PersonnelDtoOut[]>(
      `${environment.apiUrl}/admin/nurse`,
      body
    ).pipe(
      first(),
      map(data => data.map(parseNurse))
    );
  }

  public login(email: string, password: string, vaccineSerialNumber: string, nurseId: string): Observable<User> {

    const body: LoginDtoIn = {
      credentials: {
        email,
        password
      },
      vaccineSerialNumber,
      nurseId
    };

    return this._http.post<UserLoginResponseDtoOut>(
      `${environment.apiUrl}/admin/login`,
      body
    ).pipe(
      map((r: UserLoginResponseDtoOut) => {
        const token = r.token;
        const decoded = jwt_decode(token) as DecodedToken;

        // TODO: parse role, check decoded
        const user: User = { username: email, role: UserRole[r.role], token, decoded };

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
