import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { RegistrationConfirmation } from '@app/model/RegistrationConfirmation';
import { VaccinationLocation } from '@app/model/VaccinationLocation';
import { LocationDtoOut } from '@app/generated';
import { environment } from '@environments/environment';
import { map } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';
import { parseLocation } from '@app/parsers/location.parser';

const test: RegistrationConfirmation = {
  from: new Date('2021-06-07T15:09:00Z'),
  id: 'e74e3548-ab91-4be8-8abc-9f5c3b633352',
  locationId: '9ed94eda-9c4c-469f-905d-a3ccd0ab3d60',
  patientId: 'f50e2fda-bf3e-47ed-895c-8af4e0ae8357',
  queue: 1,
  to: new Date('2021-06-07T15:12:00Z')
};

@Injectable({
  providedIn: 'root'
})
export class ConfirmationService {

  // todo
  private _confirmationSubject: BehaviorSubject<RegistrationConfirmation | undefined> = new BehaviorSubject<RegistrationConfirmation | undefined>(test);
  public confirmationObservable: Observable<RegistrationConfirmation | undefined> = this._confirmationSubject.asObservable();

  constructor(private _http: HttpClient) {
  }

  get registrationConfirmation(): RegistrationConfirmation | undefined {
    return this._confirmationSubject.value;
  }

  set registrationConfirmation(value: RegistrationConfirmation | undefined) {
    this._confirmationSubject.next(value);
  }

  public async getLocation(locationId: string): Promise<VaccinationLocation> {
    return this._http.get<LocationDtoOut>(
      `${environment.apiUrl}/locations/${locationId}`
    ).pipe(
      map(parseLocation)
    ).toPromise();
  }
}
