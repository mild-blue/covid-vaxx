import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { PatientService } from '@app/services/patient/patient.service';
import { environment } from '@environments/environment';
import { PatientData } from '@app/model/PatientData';
import { ConfirmationService } from '@app/services/confirmation/confirmation.service';
import { RegistrationConfirmation } from '@app/model/RegistrationConfirmation';
import { AlertService } from '@app/services/alert/alert.service';
import { VaccinationLocation } from '@app/model/VaccinationLocation';

@Component({
  selector: 'app-registration-done',
  templateUrl: './registration-done.component.html',
  styleUrls: ['./registration-done.component.scss']
})
export class RegistrationDoneComponent implements OnInit {

  public patientData?: PatientData;
  public confirmation?: RegistrationConfirmation;
  public location?: VaccinationLocation;

  public loading: boolean = false;
  public companyEmail: string = environment.companyEmail;

  constructor(private _router: Router,
              private _confirmationService: ConfirmationService,
              private _patientService: PatientService,
              private _alertService: AlertService) {
    this._patientService.patientObservable.subscribe(patient => this.patientData = patient);
    this._confirmationService.confirmationObservable.subscribe(confirmation => this.confirmation = confirmation);
  }

  ngOnInit(): void {
    this._initLocation();
  }

  handleStartAgain() {
    this._router.navigate(['/registration']);
  }

  private async _initLocation() {
    if (!this.confirmation) {
      return;
    }

    this.loading = true;
    try {
      this.location = await this._confirmationService.getLocation(this.confirmation.locationId);
    } catch (e) {
      this._alertService.error(e.message);
    } finally {
      this.loading = false;
    }
  }
}
