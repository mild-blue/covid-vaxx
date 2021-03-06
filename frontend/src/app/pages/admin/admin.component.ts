import { Component, OnInit } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { validatePersonalNumber } from '@app/validators/form.validators';
import { AlertService } from '@app/services/alert/alert.service';
import { PatientService } from '@app/services/patient/patient.service';
import { Patient } from '@app/model/Patient';

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.scss']
})
export class AdminComponent implements OnInit {

  public personalNumber: FormControl = new FormControl('', [Validators.required, validatePersonalNumber]);
  public patients: Patient[] = [];

  constructor(private _alertService: AlertService,
              private _patientService: PatientService) {
  }

  ngOnInit(): void {
  }

  public async onSubmit(): Promise<void> {
    if (this.personalNumber.invalid) {
      return;
    }

    try {
      const patient = await this._patientService.findPatientByPersonalNumber(this.personalNumber.value);
    } catch (e) {
      this._alertService.toast(e.message);
    }
  }

}
