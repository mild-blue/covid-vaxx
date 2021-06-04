import { Injectable } from '@angular/core';
import { environment } from '@environments/environment';
import { first, map } from 'rxjs/operators';
import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Patient } from '@app/model/Patient';
import { parsePatient } from '@app/parsers/patient.parser';
import { QuestionService } from '@app/services/question/question.service';
import { PatientDtoOut, VaccinationSlotDtoOut } from '@app/generated';
import { PatientData } from '@app/model/PatientData';
import { fromPatientToRegistrationGenerated, fromPatientToUpdateGenerated } from '@app/parsers/to-generated/patient.parser';
import { BodyPart } from '@app/model/enums/BodyPart';
import { BehaviorSubject, Observable } from 'rxjs';
import { RegistrationConfirmation } from '@app/model/RegistrationConfirmation';
import { parseVaccinationSlotToRegistrationConfirmation } from '@app/parsers/registration.parser';

@Injectable({
  providedIn: 'root'
})
export class PatientService {

  private _patientSubject: BehaviorSubject<PatientData | undefined> = new BehaviorSubject<PatientData | undefined>(undefined);
  public patientObservable: Observable<PatientData | undefined> = this._patientSubject.asObservable();

  constructor(private _http: HttpClient,
              private _questionService: QuestionService) {
  }

  get patient(): PatientData | undefined {
    return this._patientSubject.value;
  }

  set patient(p: PatientData | undefined) {
    this._patientSubject.next(p);
  }

  public async savePatientInfo(token: string, patient: PatientData, agreement: boolean, confirmation: boolean, gdpr: boolean): Promise<RegistrationConfirmation> {
    const params = new HttpParams().set('captcha', token);
    this._patientSubject.next(patient);

    return this._http.post<VaccinationSlotDtoOut>(
      `${environment.apiUrl}/patient`,
      fromPatientToRegistrationGenerated(patient, agreement, confirmation, gdpr),
      { params }
    ).pipe(
      map(parseVaccinationSlotToRegistrationConfirmation)
    ).toPromise();
  }

  public async findPatientByPersonalNumber(personalNumber: string): Promise<Patient> {
    const params = new HttpParams().set('personalNumber', personalNumber.trim());

    return this._http.get<PatientDtoOut>(
      `${environment.apiUrl}/admin/patient`,
      { params }
    ).pipe(
      map(data => {
        const questions = this._questionService.questionsValue;
        return parsePatient(data, questions);
      })
    ).toPromise();
  }

  public async findPatientById(id: string): Promise<Patient> {
    return this._http.get<PatientDtoOut>(
      `${environment.apiUrl}/admin/patient/${id}`
    ).pipe(
      map(data => {
        const questions = this._questionService.questionsValue;
        return parsePatient(data, questions);
      })
    ).toPromise();
  }

  public async updatePatient(patient: Patient): Promise<HttpResponse<unknown>> {
    return this._http.put<HttpResponse<unknown>>(
      `${environment.apiUrl}/admin/patient/${patient.id}`,
      fromPatientToUpdateGenerated(patient)
    ).pipe(first()).toPromise();
  }

  public async confirmVaccination(id: string, bodyPart: BodyPart, note: string): Promise<HttpResponse<unknown>> {
    const now = new Date();

    // we do not care about time, just about date
    now.setUTCHours(0, 0, 0, 0);

    return this._http.post<HttpResponse<unknown>>(
      `${environment.apiUrl}/admin/vaccination`,
      {
        patientId: id,
        bodyPart: bodyPart.valueOf(),
        notes: note,
        vaccinatedOn: now.toISOString()
      }
    ).pipe(
      first()
    ).toPromise();
  }

  public async verifyPatient(patient: Patient, note: string): Promise<HttpResponse<unknown>> {
    return this._http.post<HttpResponse<unknown>>(
      `${environment.apiUrl}/admin/data-correctness`,
      {
        dataAreCorrect: true,
        notes: note,
        patientId: patient.id
      }
    ).pipe(first()).toPromise();
  }
}
