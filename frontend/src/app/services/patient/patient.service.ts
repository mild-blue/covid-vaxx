import { Injectable } from '@angular/core';
import { environment } from '@environments/environment';
import { first, map } from 'rxjs/operators';
import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Patient } from '@app/model/Patient';
import { parsePatient } from '@app/parsers/patient.parser';
import { QuestionService } from '@app/services/question/question.service';
import { PatientDtoOut, PatientRegistrationDtoIn } from '@app/generated';
import { Question } from '@app/model/Question';
import { fromQuestionToAnswerGenerated } from '@app/parsers/to-generated/answer.parser';
import { PatientData } from '@app/model/PatientData';
import { fromInsuranceToInsuranceGenerated } from '@app/parsers/to-generated/insurance.parse';
import { fromPatientToGenerated } from '@app/parsers/to-generated/patient.parser';

@Injectable({
  providedIn: 'root'
})
export class PatientService {

  private _sessionStorageKey: string = 'patientData';

  constructor(private _http: HttpClient,
              private _questionService: QuestionService) {
  }

  public async savePatientInfo(token: string, patientInfo: PatientData, questions: Question[], agreement: boolean, confirmation: boolean, gdpr: boolean): Promise<HttpResponse<unknown>> {
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

    return this._http.post<HttpResponse<unknown>>(
      `${environment.apiUrl}/patient`,
      registration,
      { params }
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
    const retrieveData = sessionStorage.getItem(this._sessionStorageKey);
    let patientData;

    if (retrieveData) {
      patientData = JSON.parse(retrieveData);
    }
    return patientData;
  }

  public saveToStorage(patientInfo: PatientData): void {
    sessionStorage.setItem(this._sessionStorageKey, JSON.stringify(patientInfo));
  }

  public deleteFromStorage(): void {
    sessionStorage.removeItem(this._sessionStorageKey);
  }

  // public confirmVaccination(id: string, token: string): void{
  //   const params = new HttpParams().set('captcha', token);

  public async confirmVaccination(id: string): Promise<HttpResponse<unknown>> {
    // const params = new HttpParams().set('captcha', token);
    //const params = new HttpParams().set('vaccinatedOn', new Date().getTime().toString());

    return this._http.put<HttpResponse<unknown>>(
      `${environment.apiUrl}/admin/patient/single/${id}`,
      { vaccinatedOn: new Date().toISOString() }
    ).pipe(
      first()
    ).toPromise();
  }

  public async findPatientById(id: string): Promise<Patient> {
    return this._http.get<PatientDtoOut>(
      `${environment.apiUrl}/admin/patient/single/${id}`
    ).pipe(
      map(data => {
        const questions = this._questionService.questions;
        return parsePatient(data, questions);
      })
    ).toPromise();
  }

  public async updatePatient(patient: Patient): Promise<HttpResponse<unknown>> {
    return this._http.put<HttpResponse<unknown>>(
      `${environment.apiUrl}/admin/patient/single/${patient.id}`,
      fromPatientToGenerated(patient)
    ).pipe(first()).toPromise();
  }
}
