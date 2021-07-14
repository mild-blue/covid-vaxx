import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PatientService } from '@app/services/patient/patient.service';
import { AlertService } from '@app/services/alert/alert.service';
import { AdminPatientAbstractComponent } from '@app/pages/admin/abstract/admin-patient-abstract.component';
import { ConfirmVaccinationComponent } from '@app/components/dialogs/confirm-vaccination/confirm-vaccination.component';
import { ConfirmPatientDataComponent } from '@app/components/dialogs/confirm-patient-data/confirm-patient-data.component';
import { VaccinationConfirmation } from '@app/model/VaccinationConfirmation';

@Component({
  selector: 'app-patient-detail',
  templateUrl: './admin-patient.component.html',
  styleUrls: ['./admin-patient.component.scss']
})
export class AdminPatientComponent extends AdminPatientAbstractComponent implements OnInit {

  constructor(private _router: Router,
              private _route: ActivatedRoute,
              private _patientService: PatientService,
              private _alertService: AlertService) {
    super(_route, _patientService, _alertService);
  }

  async ngOnInit(): Promise<void> {
    await super.ngOnInit();
  }

  public searchAgain(): void {
    this._router.navigate(['admin']);
  }

  public verify(): void {
    this._alertService.confirmDialog(ConfirmPatientDataComponent, this._handleVerification.bind(this));
  }

  public vaccinate(doseNumber: 1 | 2): void {
    this._alertService.confirmDialog(
      ConfirmVaccinationComponent,
      async confirmation => this._handleConfirmation(confirmation, doseNumber)
    );
  }

  private async _handleVerification(note: string): Promise<void> {
    if (!this.patient) {
      return;
    }

    this.loading = true;
    try {
      await this._patientService.verifyPatient(this.patient, note);
      this._alertService.successDialog('Údaje pacienta byly ověřeny.');
      this.patient.verified = true;
    } catch (e) {
      this._alertService.error(e.message);
    } finally {
      this.loading = false;
    }
  }

  private async _handleConfirmation(confirmation: VaccinationConfirmation, doseNumber: 1 | 2): Promise<void> {
    if (!this.patient) {
      return;
    }

    this.loading = true;
    try {
      await this._patientService.confirmVaccination(this.patient.id, confirmation.bodyPart, confirmation.note, doseNumber);
      this._alertService.successDialog('Očkování bylo zaznamenáno.', this.initPatient.bind(this));
    } catch (e) {
      this._alertService.error(e.message);
    } finally {
      this.loading = false;
    }
  }
}
