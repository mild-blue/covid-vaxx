import { Component, ViewChild } from '@angular/core';
import { FormBuilder, NgForm } from '@angular/forms';
import { InsuranceCompany } from '@app/model/InsuranceCompany';
import { QuestionService } from '@app/services/question/question.service';
import { PatientService } from '@app/services/patient/patient.service';
import { AlertService } from '@app/services/alert/alert.service';
import { ActivatedRoute, Router } from '@angular/router';
import { PatientData } from '@app/model/PatientData';
import { ReCaptchaV3Service } from 'ng-recaptcha';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent {

  @ViewChild('patientForm') patientForm?: NgForm;

  public patient?: PatientData;
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
    this.patient = this._initPatient();

    const personalNumber = this._route.snapshot.paramMap.get('personalNumber');
    if (personalNumber) {
      this.patient.personalNumber = personalNumber;
    }
  }

  get allQuestionsAnswered(): boolean {
    if (!this.patient) {
      return false;
    }
    const unanswered = this.patient.questionnaire.filter(q => q.answer === undefined);
    return unanswered.length === 0;
  }

  private _initPatient(): PatientData {
    return {
      firstName: '',
      lastName: '',
      personalNumber: '',
      insuranceCompany: undefined,
      phoneNumber: '',
      email: '',
      postalCode: '',
      district: '',
      questionnaire: this._questionService.questions
    };
  }

  public openGdprInfo(): void {
    this._alertService.gdprDialog();
  }

  public async submit(): Promise<void> {
    if (!this.patient) {
      return;
    }

    try {
      const token = await this._recaptchaV3Service.execute('patientRegistration').toPromise();
      await this._patientService.savePatientInfo(
        token,
        this.patient,
        this.agreementCheckboxValue,
        this.confirmationCheckboxValue,
        this.gdprCheckboxValue
      );
      this._router.navigate(['/registration-done']);
    } catch (e) {
      this._alertService.error(e.message);
    }
  }
}
