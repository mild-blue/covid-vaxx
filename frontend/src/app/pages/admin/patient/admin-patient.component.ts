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

  constructor(private _router: Router,
              private _route: ActivatedRoute,
              private _patientService: PatientService,
              private _alertService: AlertService) {
    super(_route, _patientService, _alertService);
  }

  ngOnInit(): void {
    super.ngOnInit();
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
