import { Component, OnInit } from '@angular/core';
import { QuestionService } from '@app/services/question/question.service';
import { AlertService } from '@app/services/alert/alert.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  constructor(private _questionService: QuestionService,
              private _alertService: AlertService) {
  }

  async ngOnInit(): Promise<void> {
    // Reload questions from BE on every app load (page refresh as well)
    await this._initQuestions();
  }

  private async _initQuestions(): Promise<void> {
    try {
      await this._questionService.loadQuestions();
    } catch (e) {
      this._alertService.toast(e.message);
    }
  }
}
