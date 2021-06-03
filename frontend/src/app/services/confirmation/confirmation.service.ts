import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { RegistrationConfirmation } from '@app/model/RegistrationConfirmation';

@Injectable({
  providedIn: 'root'
})
export class ConfirmationService {

  private _confirmationSubject: BehaviorSubject<RegistrationConfirmation | undefined> = new BehaviorSubject<RegistrationConfirmation | undefined>(undefined);
  public confirmationObservable: Observable<RegistrationConfirmation | undefined> = this._confirmationSubject.asObservable();

  constructor() {
  }

  get registrationConfirmation(): RegistrationConfirmation | undefined {
    return this._confirmationSubject.value;
  }

  set registrationConfirmation(value: RegistrationConfirmation | undefined) {
    this._confirmationSubject.next(value);
  }
}
