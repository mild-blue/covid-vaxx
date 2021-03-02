import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { PatientInfo } from './model/PatientInfo';
import { InsuranceCompany } from './model/InsuranceCompany';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  isLinear = false;
  basicInfoForm: FormGroup;

  public patientInfo = new PatientInfo();
  public allInsuranceCompanies: string[] = Object.values(InsuranceCompany);

  public agreementCheckboxValue: boolean = false;
  public confirmationCheckboxValue: boolean = false;

  constructor(private _formBuilder: FormBuilder) {
  }

  ngOnInit() {
    this.basicInfoForm = this._formBuilder.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      birthNumber: ['', Validators.required], // todo: validate
      insuranceCompany: ['', Validators.required],
      phone: ['', [Validators.required]], // todo: validate phone
      email: ['', [Validators.required, Validators.email]]
    });
  }

  public submit() {
    console.log('submit', this.patientInfo);
    if (!(this.basicInfoForm.valid && this.allQuestionsAnswered)) {
      return;
    }
  }

  get allQuestionsAnswered(): boolean {
    const unanswered = this.patientInfo.questions.filter(q => q.value === undefined);
    return unanswered.length === 0;
  }
}
