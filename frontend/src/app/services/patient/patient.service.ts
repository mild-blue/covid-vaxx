import { Injectable } from '@angular/core';
import { PatientInfo, YesNoQuestion } from '@app/model/PatientInfo';
import { environment } from '@environments/environment';
import { first, map } from 'rxjs/operators';
import { HttpClient, HttpParams } from '@angular/common/http';
import { PatientResponse } from '@app/services/patient/patient.interface';
import { Patient, PatientOut } from '@app/model/Patient';
import { parsePatient } from '@app/parsers/patient.parser';

@Injectable({
  providedIn: 'root'
})
export class PatientService {

  constructor(private _http: HttpClient) {
  }

  public async savePatientInfo(patientInfo: PatientInfo, questions: YesNoQuestion[], agreement: boolean, confirmation: boolean): Promise<PatientResponse> {
    return this._http.post<PatientResponse>(
      `${environment.apiUrl}/patient`,
      {
        answers: questions.map(q => {
          return { questionId: q.id, value: q.value };
        }),
        confirmation: {
          covid19VaccinationAgreement: agreement,
          healthStateDisclosureConfirmation: confirmation
        },
        ...patientInfo
      }
    ).pipe(
      first()
    ).toPromise();
  }

  public async findPatientByPersonalNumber(personalNumber: string): Promise<Patient[]> {
    const params = new HttpParams().set('personalNumber', personalNumber);

    return this._http.get<PatientOut[]>(
      `${environment.apiUrl}/patient`,
      { params }
    ).pipe(
      map(data => data.map(parsePatient))
    ).toPromise();
  }
}
