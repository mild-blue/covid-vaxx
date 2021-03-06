import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Question } from '@app/model/Question';
import { environment } from '@environments/environment';
import { first, map } from 'rxjs/operators';
import { parseQuestion } from '@app/parsers/question.parser';
import { YesNoQuestion } from '@app/model/PatientInfo';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class QuestionService {

  private _questionKey = 'questions';
  private _questionsSubject: BehaviorSubject<YesNoQuestion[]> = new BehaviorSubject<YesNoQuestion[]>([]);

  constructor(private _http: HttpClient) {
    this._initQuestions();
  }

  get questions(): YesNoQuestion[] {
    return this._questionsSubject.value;
  }

  public async loadQuestions(): Promise<YesNoQuestion[]> {
    localStorage.removeItem(this._questionKey);

    return this._http.get<Question[]>(
      `${environment.apiUrl}/question`
    ).pipe(
      first(),
      map(response => {
        const questions = response.map(parseQuestion);

        this._questionsSubject.next(questions);
        localStorage.setItem(this._questionKey, JSON.stringify(questions));

        return questions;
      })
    ).toPromise();
  }

  private _initQuestions(): void {
    const value = localStorage.getItem(this._questionKey);
    const savedQuestions = value ? JSON.parse(value) : [];
    this._questionsSubject.next(savedQuestions);
  }
}
