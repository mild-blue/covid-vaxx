import { Component, KeyValueDiffers, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PatientService } from '@app/services/patient/patient.service';
import { AlertService } from '@app/services/alert/alert.service';
import { AdminPatientAbstractComponent } from '@app/pages/admin/abstract/admin-patient-abstract.component';
import { Patient } from '@app/model/Patient';

@Component({
  selector: 'app-edit-patient',
  templateUrl: './admin-edit.component.html',
  styleUrls: ['./admin-edit.component.scss']
})
export class AdminEditComponent extends AdminPatientAbstractComponent implements OnInit {

  private _patientBase?: Patient;

  constructor(private _route: ActivatedRoute,
              private _router: Router,
              private _patientService: PatientService,
              private _alertService: AlertService,
              private _differs: KeyValueDiffers) {
    super(_route, _patientService, _alertService);
  }

  async ngOnInit(): Promise<void> {
    await super.ngOnInit();

    if (this.patient) {
      // Create deep copy of patient
      this._patientBase = { ...this.patient };
      // Create deep copy of answers
      this._patientBase.questionnaire = this.patient.questionnaire.map(a => ({ ...a }));
    }
  }

  get patientChanged(): boolean {
    return JSON.stringify(this.patient) !== JSON.stringify(this._patientBase);
  }

  public async handleSave(): Promise<void> {
    if (!this.patient) {
      return;
    }

    try {
      await this._patientService.updatePatient(this.patient);
      this._alertService.successDialog('Data pacienta byla úspěšně uložena.', this._routeBack.bind(this));
    } catch (e) {
      this._alertService.error(e.message);
    }
  }

  private _routeBack(): void {
    this._router.navigate(['/admin/patient', this.id]);
  }
}
