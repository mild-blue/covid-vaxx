import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { PatientInfo } from './model/PatientInfo';
import { InsuranceCompany } from './model/InsuranceCompany';
import { MatDialog } from '@angular/material/dialog';
import { DialogComponent } from './components/dialog/dialog.component';
import { PatientService } from './services/patient.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  public basicInfoForm: FormGroup;

  public patientInfo = new PatientInfo();
  public allInsuranceCompanies: string[] = Object.values(InsuranceCompany);

  public agreementCheckboxValue: boolean = false;
  public confirmationCheckboxValue: boolean = false;

  constructor(private _formBuilder: FormBuilder,
              private _dialog: MatDialog,
              private _patientService: PatientService) {
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

  get allQuestionsAnswered(): boolean {
    const unanswered = this.patientInfo.questions.filter(q => q.value === undefined);
    return unanswered.length === 0;
  }

  get canSubmit(): boolean {
    return this.basicInfoForm.valid && this.allQuestionsAnswered && this.agreementCheckboxValue && this.confirmationCheckboxValue;
  }

  public submit() {
    if (!this.canSubmit) {
      return;
    }

    this._patientService.savePatientInfo(this.patientInfo).then(res => {
      this.openDialog();
    });
  }

  public openDialog(): void {
    const dialogRef = this._dialog.open(DialogComponent, {
      width: '250px',
      data: this.patientInfo
    });
  }
}
