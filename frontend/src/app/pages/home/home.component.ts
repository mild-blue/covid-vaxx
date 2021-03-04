import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PatientInfo, YesNoQuestion } from '@app/model/PatientInfo';
import { InsuranceCompany } from '@app/model/InsuranceCompany';
import { MatDialog } from '@angular/material/dialog';
import { QuestionService } from '@app/services/question/question.service';
import { PatientService } from '@app/services/patient/patient.service';
import { validatePersonalNumber, validatePhoneNumber } from '@app/validators/form.validators';
import { DialogComponent } from '@app/components/dialog/dialog.component';

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
              private _dialog: MatDialog,
              private _questionService: QuestionService,
              private _patientService: PatientService) {
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
    this.questions = await this._questionService.getQuestions();
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

  public submit() {
    if (!this.canSubmit) {
      return;
    }

    this._patientService.savePatientInfo(this.patientInfo, this.questions, this.agreementCheckboxValue, this.confirmationCheckboxValue).then((result) => {
      if (result.patientId) {
        this.openDialog();
      }
    });
  }

  public openDialog(): void {
    this._dialog.open(DialogComponent, {
      width: '250px',
      data: this.patientInfo
    });
  }

}
