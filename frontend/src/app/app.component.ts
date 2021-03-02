import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { PatientInfo } from './model/PatientInfo';
import { InsuranceCompany } from './model/InsuranceCompany';
import { MatDialog } from '@angular/material/dialog';
import { DialogComponent } from './components/dialog/dialog.component';

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

  constructor(private _formBuilder: FormBuilder,
              public _dialog: MatDialog) {
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
    if (!this.canSubmit) {
      return;
    }

    // todo send data to BE
    this.openDialog();
  }

  public openDialog(): void {
    const dialogRef = this._dialog.open(DialogComponent, {
      width: '250px',
      data: this.patientInfo
    });
  }

  get allQuestionsAnswered(): boolean {
    const unanswered = this.patientInfo.questions.filter(q => q.value === undefined);
    return unanswered.length === 0;
  }

  get canSubmit(): boolean {
    return this.basicInfoForm.valid && this.allQuestionsAnswered && this.agreementCheckboxValue && this.confirmationCheckboxValue;
  }
}
