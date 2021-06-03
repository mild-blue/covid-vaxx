import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { PatientService } from '@app/services/patient/patient.service';
import { environment } from '@environments/environment';
import { PatientData } from '@app/model/PatientData';
import { ConfirmationService } from '@app/services/confirmation/confirmation.service';
import { RegistrationConfirmation } from '@app/model/RegistrationConfirmation';

@Component({
  selector: 'app-registration-done',
  templateUrl: './registration-done.component.html',
  styleUrls: ['./registration-done.component.scss']
})
export class RegistrationDoneComponent implements OnInit {

  public patientData?: PatientData;
  public confirmation?: RegistrationConfirmation;
  public companyEmail: string = environment.companyEmail;

  constructor(private _router: Router,
              private _confirmationService: ConfirmationService,
              private _patientService: PatientService) {
    this._patientService.patientObservable.subscribe(patient => this.patientData = patient);
    this._confirmationService.confirmationObservable.subscribe(confirmation => this.confirmation = confirmation);
  }

  ngOnInit(): void {
    // this._registerPatient();
  }

  handleStartAgain() {
    this._router.navigate(['/registration']);
  }
}
