import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {PatientInfo} from './model/PatientInfo';
import {InsuranceCompany} from './model/InsuranceCompany';
import {MatDialog} from '@angular/material/dialog';
import {DialogComponent} from './components/dialog/dialog.component';
import {PatientService} from './services/patient.service';
import {validatePersonalNumber, validatePhoneNumber} from './app.validators';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  constructor(
    private formBuilder: FormBuilder,
    private dialog: MatDialog,
    private patientService: PatientService
  ) {
  }

  get allQuestionsAnswered(): boolean {
    const unanswered = this.patientInfo.questions.filter(q => q.value === undefined);
    return unanswered.length === 0;
  }

  get canSubmit(): boolean {
    return this.basicInfoForm.valid && this.allQuestionsAnswered && this.agreementCheckboxValue && this.confirmationCheckboxValue;
  }

  public basicInfoForm: FormGroup;

  public patientInfo = new PatientInfo();
  public allInsuranceCompanies: string[] = Object.values(InsuranceCompany);

  public agreementCheckboxValue = false;
  public confirmationCheckboxValue = false;


  ngOnInit() {
    this.basicInfoForm = this.formBuilder.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      personalNumber: ['', [Validators.required, validatePersonalNumber]], // todo: validate
      insuranceCompany: ['', Validators.required],
      phone: ['', [Validators.required, validatePhoneNumber]], // todo: validate phone
      email: ['', [Validators.required, Validators.email]]
    });
  }

  public submit() {
    if (!this.canSubmit) {
      return;
    }

    this.patientService.savePatientInfo(this.patientInfo).then(res => {
      this.openDialog();
    });
  }

  public openDialog(): void {
    const dialogRef = this.dialog.open(DialogComponent, {
      width: '250px',
      data: this.patientInfo
    });
  }
}
