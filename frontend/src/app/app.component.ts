import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { PatientInfo } from './model/PatientInfo';

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

  constructor(private _formBuilder: FormBuilder) {}

  ngOnInit() {
    this.firstFormGroup = this._formBuilder.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      insurance: ['', Validators.required],
      phone: ['', [Validators.required]], // todo: validate phone
      email: ['', [Validators.required, Validators.email]]
    });
    this.secondFormGroup = this._formBuilder.group({
      secondCtrl: ['', Validators.required]
    });
  }

  public submit() {
    if(this.firstFormGroup.invalid) {
      return;
    }
    console.log('submit', this.patientInfo);
  }
}
