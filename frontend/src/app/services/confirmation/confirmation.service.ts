import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { RegistrationConfirmation } from '@app/model/RegistrationConfirmation';
import { VaccinationLocation } from '@app/model/VaccinationLocation';
import { LocationDtoOut } from '@app/generated';
import { environment } from '@environments/environment';
import { map } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';
import { parseLocation } from '@app/parsers/location.parser';

@Injectable({
  providedIn: 'root'
})
export class ConfirmationService {

  private _confirmationSubject: BehaviorSubject<RegistrationConfirmation | undefined> = new BehaviorSubject<RegistrationConfirmation | undefined>(undefined);
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
