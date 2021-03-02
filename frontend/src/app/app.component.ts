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
  firstFormGroup: FormGroup;
  secondFormGroup: FormGroup;

  public patientInfo = new PatientInfo();
  public allInsuranceCompanies: string[] = Object.values(InsuranceCompany);

  constructor(private _formBuilder: FormBuilder) {}

  ngOnInit() {
    this.firstFormGroup = this._formBuilder.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      birthNumber: ['', Validators.required], // todo: validate
      insuranceCompany: ['', Validators.required],
      phone: ['', [Validators.required]], // todo: validate phone
      email: ['', [Validators.required, Validators.email]]
    });
    this.secondFormGroup = this._formBuilder.group({
      isSick: ['', Validators.required]
    });
  }

  public submit() {
    // if(this.firstFormGroup.invalid) {
    //   return;
    // }
    console.log('submit', this.patientInfo);
  }
}
