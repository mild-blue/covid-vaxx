import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Question } from '@app/model/Question';
import { environment } from '@environments/environment';
import { first, map } from 'rxjs/operators';
import { parseQuestion } from '@app/parsers/question.parser';
import { YesNoQuestion } from '@app/model/PatientInfo';

@Injectable({
  providedIn: 'root'
})
export class QuestionService {

  constructor(private _http: HttpClient) {
  }

  public async getQuestions(): Promise<YesNoQuestion[]> {
    return this._http.get<Question[]>(
      `${environment.apiUrl}/question`
    ).pipe(
      first(),
      map(response => response.map(parseQuestion))
    ).toPromise();
  }
}
