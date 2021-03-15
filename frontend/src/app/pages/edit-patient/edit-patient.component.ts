import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Patient } from '@app/model/Patient';
import { PatientService } from '@app/services/patient/patient.service';
import { AlertService } from '@app/services/alert/alert.service';

@Component({
  selector: 'app-edit-patient',
  templateUrl: './edit-patient.component.html',
  styleUrls: ['./edit-patient.component.scss']
})
export class EditPatientComponent implements OnInit {

  public patient?: Patient;

  constructor(private _route: ActivatedRoute,
              private _patientService: PatientService,
              private _alertService: AlertService) {
  }

  ngOnInit(): void {
    const id = this._route.snapshot.paramMap.get('id');
    console.log(id);
    if (id) {
      this._initPatient(id);
    } else {
      this._alertService.toast('Patient ID is not specified');
    }
  }

  private async _initPatient(id: string): Promise<void> {
    try {
      this.patient = await this._patientService.findPatientById(id);
      console.log('Patient', this.patient);
    } catch (e) {
      this._alertService.toast(e.message);
    }
  }
}
