import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PatientService } from '@app/services/patient/patient.service';
import { AlertService } from '@app/services/alert/alert.service';
import { AdminAbstractComponent } from '@app/pages/admin/abstract/admin-abstract.component';

@Component({
  selector: 'app-patient-detail',
  templateUrl: './admin-detail.component.html',
  styleUrls: ['./admin-detail.component.scss']
})
export class AdminDetailComponent extends AdminAbstractComponent implements OnInit {

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
}
