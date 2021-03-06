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

  // eslint-disable-next-line no-magic-numbers
  private _searchHistoryLimit = 5;
  private _searchHistoryKey: string = 'searchHistory';

  constructor(private _http: HttpClient) {
  }

  get searchHistory(): string[] {
    const value = localStorage.getItem(this._searchHistoryKey);
    return value ? JSON.parse(value) : [];
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
    const params = new HttpParams();
    params.set('personalNumber', personalNumber);

    return this._http.get<PatientOut[]>(
      `${environment.apiUrl}/patient`,
      { params }
    ).pipe(
      map(data => data.map(parsePatient))
    ).toPromise();
  }

  public saveSearch(search: string): void {
    const storageValues = localStorage.getItem(this._searchHistoryKey);

    if (!storageValues) {
      localStorage.setItem(this._searchHistoryKey, JSON.stringify([search]));
      return;
    }

    const searchHistory = JSON.parse(storageValues) as string[];
    searchHistory.unshift(search);
    searchHistory.slice(0, this._searchHistoryLimit);

    localStorage.setItem(this._searchHistoryKey, JSON.stringify(searchHistory));
  }

  public clearHistory(): void {
    localStorage.removeItem(this._searchHistoryKey);
  }
}
