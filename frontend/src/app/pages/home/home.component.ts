import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PatientInfo, YesNoQuestion } from '@app/model/PatientInfo';
import { InsuranceCompany } from '@app/model/InsuranceCompany';
import { QuestionService } from '@app/services/question/question.service';
import { PatientService } from '@app/services/patient/patient.service';
import { validatePersonalNumber, validatePhoneNumber } from '@app/validators/form.validators';
import { AlertService } from '@app/services/alert/alert.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

  public basicInfoForm?: FormGroup;

  public patientInfo: PatientInfo = new PatientInfo();
  public questions: YesNoQuestion[] = [];
  public allInsuranceCompanies: string[] = Object.values(InsuranceCompany);

  public agreementCheckboxValue: boolean = false;
  public confirmationCheckboxValue: boolean = false;

  constructor(private _formBuilder: FormBuilder,
              private _questionService: QuestionService,
              private _patientService: PatientService,
              private _alertService: AlertService) {
  }

  ngOnInit() {
    this.basicInfoForm = this._formBuilder.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      personalNumber: ['', [Validators.required, validatePersonalNumber]],
      insuranceCompany: ['', Validators.required],
      phoneNumber: ['', [Validators.required, validatePhoneNumber]],
      email: ['', [Validators.required, Validators.email]]
    });

    this._initQuestions();
  }

  private async _initQuestions(): Promise<void> {
    try {
      this.questions = await this._questionService.getQuestions();
    } catch (e) {
      this._alertService.toast(e.message);
    }
  }

  get allQuestionsAnswered(): boolean {
    const unanswered = this.questions.filter(q => q.value === undefined);
    return unanswered.length === 0;
  }

  get canProceedToStep2(): boolean {
    return !!this.basicInfoForm?.valid;
  }

  get canProceedToStep3(): boolean {
    return !!this.basicInfoForm?.valid && this.allQuestionsAnswered;
  }

  get canSubmit(): boolean {
    return !!this.basicInfoForm?.valid && this.allQuestionsAnswered && this.agreementCheckboxValue && this.confirmationCheckboxValue;
  }

  public async submit(): Promise<void> {
    if (!this.canSubmit) {
      return;
    }

    try {
      const result = await this._patientService.savePatientInfo(this.patientInfo, this.questions, this.agreementCheckboxValue, this.confirmationCheckboxValue);
      if (result.patientId) {
        this._alertService.patientRegisteredDialog();
      }
    } catch (e) {
      this._alertService.toast(e.message);
    }
  }
}
