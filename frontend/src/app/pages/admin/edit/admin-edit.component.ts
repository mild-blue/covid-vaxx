import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PatientService } from '@app/services/patient/patient.service';
import { AlertService } from '@app/services/alert/alert.service';
import { AdminPatientAbstractComponent } from '@app/pages/admin/abstract/admin-patient-abstract.component';

@Component({
  selector: 'app-edit-patient',
  templateUrl: './admin-edit.component.html',
  styleUrls: ['./admin-edit.component.scss']
})
export class AdminEditComponent extends AdminPatientAbstractComponent implements OnInit {

  constructor(private _route: ActivatedRoute,
              private _router: Router,
              private _patientService: PatientService,
              private _alertService: AlertService) {
    super(_route, _patientService, _alertService);
  }

  async ngOnInit(): Promise<void> {
    await super.ngOnInit();
  }

  public async handleSave(): Promise<void> {
    if (!this.patient) {
      return;
    }

    try {
      await this._patientService.updatePatient(this.patient);
      this._alertService.successDialog('Pacient úspěšně uložen', this._routeBack.bind(this));
    } catch (e) {
      this._alertService.toast(e.message);
    }
  }

  private _routeBack(): void {
    this._router.navigate(['/admin/patient', this.id]);
  }
}
