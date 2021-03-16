import { Component, OnInit } from '@angular/core';
import { Patient } from '@app/model/Patient';
import { ActivatedRoute } from '@angular/router';
import { PatientService } from '@app/services/patient/patient.service';
import { AlertService } from '@app/services/alert/alert.service';

@Component({ template: '' })
export class AdminPatientAbstractComponent implements OnInit {

  public loading: boolean = false;
  public id: string = '';
  public patient?: Patient;

  constructor(protected route: ActivatedRoute,
              protected patientService: PatientService,
              protected alertService: AlertService) {
  }

  async ngOnInit(): Promise<void> {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.id = id;
      await this.initPatient();
    } else {
      this.alertService.toast('Patient ID is not specified');
    }
  }

  public async initPatient(): Promise<void> {
    this.loading = true;
    try {
      this.patient = await this.patientService.findPatientById(this.id);
    } catch (e) {
      this.alertService.toast(e.message);
    } finally {
      this.loading = false;
    }
  }
}
