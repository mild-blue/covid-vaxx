import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PatientService } from '@app/services/patient/patient.service';
import { AlertService } from '@app/services/alert/alert.service';
import { AdminAbstractComponent } from '@app/pages/admin/abstract/admin-abstract.component';

@Component({
  selector: 'app-edit-patient',
  templateUrl: './admin-edit.component.html',
  styleUrls: ['./admin-edit.component.scss']
})
export class AdminEditComponent extends AdminAbstractComponent implements OnInit {

  constructor(private _route: ActivatedRoute,
              private _patientService: PatientService,
              private _alertService: AlertService) {
    super(_route, _patientService, _alertService);
  }

  ngOnInit(): void {
    super.ngOnInit();
  }

  public handleSave(): void {
    console.log('Saving patient', this.patient);
  }
}
