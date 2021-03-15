import { Component, OnDestroy, ViewChild } from '@angular/core';
import { FormBuilder, NgForm } from '@angular/forms';
import { PatientEditable } from '@app/model/PatientEditable';
import { InsuranceCompany } from '@app/model/InsuranceCompany';
import { QuestionService } from '@app/services/question/question.service';
import { PatientService } from '@app/services/patient/patient.service';
import { AlertService } from '@app/services/alert/alert.service';
import { ActivatedRoute, Router } from '@angular/router';
import { PatientData } from '@app/model/PatientData';
import { parseAnswerFromQuestion } from '@app/parsers/answer.parser';
import { Question } from '@app/model/Question';
import { Subscription } from 'rxjs';
import { parseInsuranceCompany } from '@app/parsers/insurance.parser';
import { ReCaptchaV3Service } from 'ng-recaptcha';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnDestroy {

  private _questionsSubscription?: Subscription;

  @ViewChild('patientForm') patientForm?: NgForm;

  public patient: PatientEditable = new PatientEditable();
  public questions: Question[] = [];
  public allInsuranceCompanies: string[] = Object.values(InsuranceCompany);

  public agreementCheckboxValue: boolean = false;
  public confirmationCheckboxValue: boolean = false;
  public gdprCheckboxValue: boolean = false;

  constructor(private _route: ActivatedRoute,
              private _router: Router,
              private _formBuilder: FormBuilder,
              private _questionService: QuestionService,
              private _patientService: PatientService,
              private _recaptchaV3Service: ReCaptchaV3Service,
              private _alertService: AlertService) {
    const personalNumber = this._route.snapshot.paramMap.get('personalNumber');
    if (personalNumber) {
      this.patient.personalNumber = personalNumber;
    }

    this._questionsSubscription = this._questionService.questionsObservable
    .subscribe(questions => this.questions = questions);
  }

  ngOnDestroy() {
    this._questionsSubscription?.unsubscribe();
  }

  get allQuestionsAnswered(): boolean {
    const unanswered = this.questions.filter(q => q.value === undefined);
    return unanswered.length === 0;
  }

  get canProceedToStep3(): boolean {
    return !!this.patientForm?.valid && this.allQuestionsAnswered;
  }

  get canSubmit(): boolean {
    return !!this.patientForm?.valid && this.allQuestionsAnswered && this.agreementCheckboxValue && this.confirmationCheckboxValue && this.gdprCheckboxValue;
  }

  public openGdprInfo(): void {
    this._alertService.gdprDialog();
  }

  public getPatientData(): PatientData {
    return {
      firstName: this.patient.firstName ?? '',
      lastName: this.patient.lastName ?? '',
      personalNumber: this.patient.personalNumber ?? '',
      insuranceCompany: this.patient.insuranceCompany ? parseInsuranceCompany(this.patient.insuranceCompany) : undefined,
      phoneNumber: this.patient.phoneNumber ?? '',
      email: this.patient.email ?? '',
      answers: this.questions.map(parseAnswerFromQuestion)
    };
  }

  public async submit(): Promise<void> {
    if (!this.canSubmit) {
      return;
    }

    try {
      const token = await this._recaptchaV3Service.execute('patientRegistration').toPromise();
      await this._patientService.savePatientInfo(
        token,
        this.getPatientData(),
        this.questions,
        this.agreementCheckboxValue,
        this.confirmationCheckboxValue,
        this.gdprCheckboxValue
      );
      this._router.navigate(['/registration-done']);
    } catch (e) {
      this._alertService.toast(e.message);
    }
  }
}
