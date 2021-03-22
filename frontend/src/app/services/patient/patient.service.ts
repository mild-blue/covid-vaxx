import { Injectable } from '@angular/core';
import { environment } from '@environments/environment';
import { first, map } from 'rxjs/operators';
import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Patient } from '@app/model/Patient';
import { parsePatient } from '@app/parsers/patient.parser';
import { QuestionService } from '@app/services/question/question.service';
import { PatientDtoOut } from '@app/generated';
import { PatientData } from '@app/model/PatientData';
import { fromPatientToRegistrationGenerated, fromPatientToUpdateGenerated } from '@app/parsers/to-generated/patient.parser';

@Injectable({
  providedIn: 'root'
})
export class PatientService {

  private _sessionStorageKey: string = 'patientData';

  constructor(private _http: HttpClient,
              private _questionService: QuestionService) {
  }

  public async savePatientInfo(token: string, patient: PatientData, agreement: boolean, confirmation: boolean, gdpr: boolean): Promise<HttpResponse<unknown>> {
    const params = new HttpParams().set('captcha', token);
    this.saveToStorage(patient);

    return this._http.post<HttpResponse<unknown>>(
      `${environment.apiUrl}/patient`,
      fromPatientToRegistrationGenerated(patient, agreement, confirmation, gdpr),
      { params }
    ).pipe(
      first()
    ).toPromise();
  }

  public async findPatientByPersonalNumber(personalNumber: string): Promise<Patient> {
    const params = new HttpParams().set('personalNumber', personalNumber.trim());

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

  public async confirmVaccination(id: string): Promise<HttpResponse<unknown>> {
    const now = new Date();

    // we do not care about time, just about date
    now.setUTCHours(0, 0, 0, 0);

    // TODO: Add isNonDominantHandUsed

    return this._http.put<HttpResponse<unknown>>(
      `${environment.apiUrl}/admin/patient/single/${id}`,
      { vaccinatedOn: now.toISOString() }
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
      fromPatientToUpdateGenerated(patient)
    ).pipe(first()).toPromise();
  }
}
