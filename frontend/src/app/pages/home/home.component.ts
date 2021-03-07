import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PatientEditable } from '@app/model/PatientEditable';
import { InsuranceCompany } from '@app/model/InsuranceCompany';
import { QuestionService } from '@app/services/question/question.service';
import { PatientService } from '@app/services/patient/patient.service';
import { validatePersonalNumber, validatePhoneNumber } from '@app/validators/form.validators';
import { AlertService } from '@app/services/alert/alert.service';
import { ActivatedRoute } from '@angular/router';
import { PatientData } from '@app/model/PatientData';
import { parseAnswerFromQuestion } from '@app/parsers/answer.parser';
import { parseInsuranceCompany } from '@app/parsers/patient.parser';
import { Question } from '@app/model/Question';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

  public basicInfoForm?: FormGroup;

  public patient: PatientEditable = new PatientEditable();
  public allInsuranceCompanies: string[] = Object.values(InsuranceCompany);

  public agreementCheckboxValue: boolean = false;
  public confirmationCheckboxValue: boolean = false;

  constructor(private _route: ActivatedRoute,
              private _formBuilder: FormBuilder,
              private _questionService: QuestionService,
              private _patientService: PatientService,
              private _alertService: AlertService) {
    const personalNumber = this._route.snapshot.paramMap.get('personalNumber');
    if (personalNumber) {
      this.patient.personalNumber = personalNumber;
    }
  }

  ngOnInit() {
    this.basicInfoForm = this._formBuilder.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      personalNumber: [this.patient.personalNumber ?? '', [Validators.required, validatePersonalNumber]],
      insuranceCompany: ['', Validators.required],
      phoneNumber: ['', [Validators.required, validatePhoneNumber]],
      email: ['', [Validators.required, Validators.email]]
    });
  }

  get questions(): Question[] {
    return this._questionService.questions;
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

  public getPatientData(): PatientData | undefined {
    const { firstName, lastName, personalNumber, insuranceCompany, phoneNumber, email } = this.patient;

    if (!firstName || !lastName || !personalNumber || !insuranceCompany || !phoneNumber || !email) {
      return;
    }

    return {
      firstName,
      lastName,
      personalNumber,
      insuranceCompany: parseInsuranceCompany(insuranceCompany),
      phoneNumber,
      email,
      answers: this.questions.map(parseAnswerFromQuestion)
    };
  }

  public async submit(): Promise<void> {
    if (!this.canSubmit) {
      return;
    }

    try {
      const result = await this._patientService.savePatientInfo(this.patient, this.questions, this.agreementCheckboxValue, this.confirmationCheckboxValue);
      if (result.patientId) {
        this._alertService.patientRegisteredDialog();
      }
    } catch (e) {
      this._alertService.toast(e.message);
    }
  }
}
