import { Injectable } from '@angular/core';
import { PatientInfo } from '@app/model/PatientInfo';
import { environment } from '@environments/environment';
import { first } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';
import { PatientResponse } from '@app/services/patient/patient.interface';

@Injectable({
  providedIn: 'root'
})
export class PatientService {

  constructor(private _http: HttpClient) {
  }

  public async savePatientInfo(patientInfo: PatientInfo): Promise<PatientResponse> {
    return this._http.post<PatientResponse>(
      `${environment.apiUrl}/patient`,
      patientInfo
    ).pipe(
      first()
    ).toPromise();
  }
}