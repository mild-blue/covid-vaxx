import { Injectable } from '@angular/core';
import { environment } from '@environments/environment';
import { first, map } from 'rxjs/operators';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Patient } from '@app/model/Patient';
import { parsePatient } from '@app/parsers/patient.parser';
import { QuestionService } from '@app/services/question/question.service';
import { PatientDtoOut, PatientRegisteredDtoOut, PatientRegistrationDtoIn } from '@app/generated';
import { Question } from '@app/model/Question';
import { fromQuestionToAnswerGenerated } from '@app/parsers/to-generated/answer.parser';
import { PatientData } from '@app/model/PatientData';
import { fromInsuranceToInsuranceGenerated } from '@app/parsers/to-generated/insurance.parse';

@Injectable({
  providedIn: 'root'
})
export class PatientService {

  constructor(private _http: HttpClient,
              private _questionService: QuestionService) {
  }

  public async savePatientInfo(token: string, patientInfo: PatientData, questions: Question[], agreement: boolean, confirmation: boolean, gdpr: boolean): Promise<PatientRegisteredDtoOut> {
    const headers = new HttpHeaders({
      recaptchaToken: token
    });
    const registration: PatientRegistrationDtoIn = {
      ...patientInfo,
      insuranceCompany: fromInsuranceToInsuranceGenerated(patientInfo.insuranceCompany),
      answers: questions.map(fromQuestionToAnswerGenerated),
      confirmation: {
        covid19VaccinationAgreement: agreement,
        healthStateDisclosureConfirmation: confirmation,
        gdprAgreement: gdpr
      }
    };

    return this._http.post<PatientRegisteredDtoOut>(
      `${environment.apiUrl}/patient`,
      registration,
      { headers }
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
