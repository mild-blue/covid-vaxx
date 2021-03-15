import { Component, OnInit } from '@angular/core';
import { Patient } from '@app/model/Patient';
import { ActivatedRoute } from '@angular/router';
import { PatientService } from '@app/services/patient/patient.service';
import { AlertService } from '@app/services/alert/alert.service';

@Component({ template: '' })
export class AdminAbstractComponent implements OnInit {

  public patient?: Patient;

  constructor(protected route: ActivatedRoute,
              protected patientService: PatientService,
              protected alertService: AlertService) {
  }

  ngOnInit(): void {
    console.log(this.route.params, this.route.parent?.params);
    const id = this.route.snapshot.paramMap.get('id');
    console.log(id);
    if (id) {
      this._initPatient(id);
    } else {
      this.alertService.toast('Patient ID is not specified');
    }
  }

  private async _initPatient(id: string): Promise<void> {
    try {
      this.patient = await this.patientService.findPatientById(id);
      console.log('Patient', this.patient);
    } catch (e) {
      this.alertService.toast(e.message);
    }
  }

}
