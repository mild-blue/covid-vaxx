import {Component, OnInit} from '@angular/core';
import {AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators} from '@angular/forms';
import {PatientInfo} from './model/PatientInfo';
import {InsuranceCompany} from './model/InsuranceCompany';
import {MatDialog} from '@angular/material/dialog';
import {DialogComponent} from './components/dialog/dialog.component';
import {PatientService} from './services/patient.service';

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

  private static readonly personalNumberAddingTwentyIssueYear = 4;
  private static readonly tenDigitPersonalNumberIssueYear = 54;
  private static readonly womanMonthAddition = 50;
  private static readonly unprobableMonthAddition = 20;
  public basicInfoForm: FormGroup;

  public patientInfo = new PatientInfo();
  public allInsuranceCompanies: string[] = Object.values(InsuranceCompany);

  public agreementCheckboxValue = false;
  public confirmationCheckboxValue = false;

  private static validatePhoneNumber(control: AbstractControl): ValidationErrors | null {
    const regex = /^\+\d{12}$/;
    if (!regex.test(control.value)) {
      return {personalNumberInvalid: true, valid: true};
    }
    return null;
  }

  private static validatePersonalNumber(control: AbstractControl): ValidationErrors | null {
    if (!control.value) {
      return {personalNumberInvalid: true};
    }

    let firstPart = '';
    let secondPart = '';

    const parts = control.value.split('/');
    if (parts.length === 1) {
      firstPart = control.value.substr(0, 6);
      secondPart = control.value.substr(6);
    } else {
      firstPart = parts[0];
      secondPart = parts[1];
    }

    if (firstPart.length !== 6 || isNaN(Number(firstPart)) || isNaN(Number(secondPart))) {
      return {personalNumberInvalid: true};
    }

    const year = Number(firstPart.substr(0, 2));
    let month = Number(firstPart.substr(2, 2));
    const day = Number(firstPart.substr(4, 2));

    const currentYear = (new Date()).getFullYear() % 100;

    if (year >= AppComponent.tenDigitPersonalNumberIssueYear || year <= currentYear) {
      if (secondPart.length === 4) {
        const controlDigit = Number(secondPart.substr(3, 1));
        const concatenated = Number(firstPart + secondPart);

        const moduloElevenOk = concatenated % 11 === 0;
        const withoutLastDigit = concatenated / 10;
        const moduloTenOk = (withoutLastDigit % 11) === 10 && controlDigit === 0;

        if (!moduloTenOk && !moduloElevenOk) {
          return {personalNumberInvalid: true};
        }
      } else {
        return {personalNumberInvalid: true};
      }
    } else {
      if (secondPart.length !== 3) {
        return {personalNumberInvalid: true};
      }
    }
    if (month > AppComponent.womanMonthAddition) {
      month -= AppComponent.womanMonthAddition;
    }

    if (month > AppComponent.unprobableMonthAddition) {
      if (year >= AppComponent.personalNumberAddingTwentyIssueYear) {
        month -= AppComponent.unprobableMonthAddition;
      } else {
        return {personalNumberInvalid: true};
      }
    }

    if (!AppComponent.isDateValid(year, month, day)) {
      return {personalNumberInvalid: true};
    }
    return null;
  }

  private static isDateValid(year: number, month: number, day: number): boolean {
    const fullYear = year >= AppComponent.tenDigitPersonalNumberIssueYear ? 1900 + year : 2000 + year;
    try {
      /* tslint:disable:no-unused-expression */
      new Date(fullYear, month, day);
      return true;
    } catch {
      return false;
    }
  }

  ngOnInit() {
    this.basicInfoForm = this.formBuilder.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      personalNumber: ['', [Validators.required, AppComponent.validatePersonalNumber]], // todo: validate
      insuranceCompany: ['', Validators.required],
      phone: ['', [Validators.required, AppComponent.validatePhoneNumber]], // todo: validate phone
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
