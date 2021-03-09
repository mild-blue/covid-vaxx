import { Component, OnInit } from '@angular/core';
import { QuestionService } from '@app/services/question/question.service';
import { AlertService } from '@app/services/alert/alert.service';
import { RecaptchaService } from '@app/services/recaptcha/recaptcha.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  constructor(private _questionService: QuestionService,
              private _recaptchaService: RecaptchaService,
              private _alertService: AlertService) {
  }

  async ngOnInit(): Promise<void> {
    this._recaptchaService.verifyUserWithRecaptcha();
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
