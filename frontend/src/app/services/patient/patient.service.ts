import { Injectable } from '@angular/core';
import { PatientEditable } from '@app/model/PatientEditable';
import { environment } from '@environments/environment';
import { first, map } from 'rxjs/operators';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Patient } from '@app/model/Patient';
import { parsePatient } from '@app/parsers/patient.parser';
import { QuestionService } from '@app/services/question/question.service';
import { PatientDtoOut, PatientRegisteredDtoOut } from '@app/generated';
import { Question } from '@app/model/Question';

@Injectable({
  providedIn: 'root'
})
export class PatientService {

  constructor(private _http: HttpClient,
              private _questionService: QuestionService) {
  }

  public async savePatientInfo(patientInfo: PatientEditable, questions: Question[], agreement: boolean, confirmation: boolean): Promise<PatientRegisteredDtoOut> {
    return this._http.post<PatientRegisteredDtoOut>(
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

    return this._http.get<PatientDtoOut[]>(
      `${environment.apiUrl}/patient`,
      { params }
    ).pipe(
      map(data => {
        const questions = this._questionService.questions;
        const patients = data.slice(0, 10);
        return patients.map(patient => parsePatient(patient, questions));
      })
    ).toPromise();
  }
}
