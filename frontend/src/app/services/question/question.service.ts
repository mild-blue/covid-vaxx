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

  private _questionKey = 'questions';

  constructor(private _http: HttpClient) {
  }

  get questions(): YesNoQuestion[] {
    const value = localStorage.getItem(this._questionKey);
    return value ? JSON.parse(value) : [];
  }

  public async loadQuestions(): Promise<YesNoQuestion[]> {
    localStorage.removeItem(this._questionKey);

    return this._http.get<Question[]>(
      `${environment.apiUrl}/question`
    ).pipe(
      first(),
      map(response => {
        const questions = response.map(parseQuestion);
        localStorage.setItem(this._questionKey, JSON.stringify(questions));
        return questions;
      })
    ).toPromise();
  }
}
