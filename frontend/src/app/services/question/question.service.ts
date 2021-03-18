import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '@environments/environment';
import { first, map } from 'rxjs/operators';
import { parseQuestion } from '@app/parsers/question.parser';
import { BehaviorSubject } from 'rxjs';
import { QuestionDtoOut } from '@app/generated';
import { AnsweredQuestion } from '@app/model/AnsweredQuestion';

@Injectable({
  providedIn: 'root'
})
export class QuestionService {

  private _questionKey = 'questions';
  private _questionsSubject: BehaviorSubject<AnsweredQuestion[]> = new BehaviorSubject<AnsweredQuestion[]>([]);

  constructor(private _http: HttpClient) {
    this._initQuestions();
  }

  get questions(): AnsweredQuestion[] {
    return this._questionsSubject.value;
  }

  public async loadQuestions(): Promise<AnsweredQuestion[]> {
    localStorage.removeItem(this._questionKey);

    return this._http.get<QuestionDtoOut[]>(
      `${environment.apiUrl}/questions`
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
