import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PatientService } from '@app/services/patient/patient.service';
import { AlertService } from '@app/services/alert/alert.service';
import { AdminPatientAbstractComponent } from '@app/pages/admin/abstract/admin-patient-abstract.component';

@Component({
  selector: 'app-patient-detail',
  templateUrl: './admin-patient.component.html',
  styleUrls: ['./admin-patient.component.scss']
})
export class AdminPatientComponent extends AdminPatientAbstractComponent implements OnInit {

  public loading: boolean = false;

  constructor(private _router: Router,
              private _route: ActivatedRoute,
              private _patientService: PatientService,
              private _alertService: AlertService) {
    super(_route, _patientService, _alertService);
  }

  async ngOnInit(): Promise<void> {
    this.loading = true;
    super.ngOnInit().finally(() => this.loading = false);
  }

  public searchAgain(): void {
    this._router.navigate(['admin']);
  }

  public async vaccinated(): Promise<void> {
    if (this.patient) {
      this._alertService.confirmVaccinateDialog(this.patient.id);
    }
  }
}
