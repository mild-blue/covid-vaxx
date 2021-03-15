import { Injectable } from '@angular/core';
import { environment } from '@environments/environment';
import { first, map } from 'rxjs/operators';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Patient } from '@app/model/Patient';
import { parsePatient } from '@app/parsers/patient.parser';
import { QuestionService } from '@app/services/question/question.service';
import { PatientDtoOut, PatientRegistrationDtoIn} from '@app/generated';
import { Question } from '@app/model/Question';
import { fromQuestionToAnswerGenerated } from '@app/parsers/to-generated/answer.parser';
import { PatientData } from '@app/model/PatientData';
import { fromInsuranceToInsuranceGenerated } from '@app/parsers/to-generated/insurance.parse';

@Injectable({
  providedIn: 'root'
})
export class PatientService {

  private _sessionStorageKey: string = 'patientData';

  constructor(private _http: HttpClient,
              private _questionService: QuestionService) {
  }

  public async savePatientInfo(token: string, patientInfo: PatientData, questions: Question[], agreement: boolean, confirmation: boolean, gdpr: boolean): Promise<null> {
    const params = new HttpParams().set('captcha', token);

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

    this.saveToStorage(patientInfo);

    return this._http.post<null>(
      `${environment.apiUrl}/patient`,
      registration,
      { params },
    ).pipe(
      first()
    ).toPromise();
  }

  public async findPatientByPersonalNumber(personalNumber: string): Promise<Patient> {
    const params = new HttpParams().set('personalNumber', personalNumber);

    return this._http.get<PatientDtoOut>(
      `${environment.apiUrl}/admin/patient/single`,
      { params }
    ).pipe(
      map(data => {
        const questions = this._questionService.questions;
        return parsePatient(data, questions);
      })
    ).toPromise();
  }

  public getFromStorage(): Patient | undefined {
    const retrieveData = sessionStorage.getItem(this._sessionStorageKey)
    let patientData = null; 

    if(retrieveData){
      patientData = JSON.parse(retrieveData);
    }
    return patientData; 
  }

  public saveToStorage(patientInfo: PatientData): void{
    sessionStorage.setItem(this._sessionStorageKey, JSON.stringify(patientInfo));
  }

  public deleteFromStorage(): void{
    sessionStorage.removeItem(this._sessionStorageKey);
  }
}
