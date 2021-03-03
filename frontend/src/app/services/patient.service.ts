import { Injectable } from '@angular/core';
import { PatientInfo } from '../model/PatientInfo';
import { environment } from '../../environments/environment';
import { first } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class PatientService {

  constructor(private http: HttpClient) {
  }

  public async savePatientInfo(patientInfo: PatientInfo): Promise<any> {
    return this.http.post<any>(
      `${environment.apiUrl}/patient-info`,
      patientInfo
    ).pipe(
      first()
    ).toPromise();
  }
}
