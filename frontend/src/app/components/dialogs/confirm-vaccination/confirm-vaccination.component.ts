import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { PatientService } from '@app/services/patient/patient.service';
import { Location } from '@angular/common';

@Component({
  selector: 'app-confirm-vaccination',
  templateUrl: './confirm-vaccination.component.html',
  styleUrls: ['./confirm-vaccination.component.scss']
})
export class ConfirmVaccinationComponent implements OnInit {

  constructor(@Inject(MAT_DIALOG_DATA) public data: { patientId: string; },
              private _patientService: PatientService,
              private _router: Router,
              private _location: Location) {
  }

  ngOnInit(): void {
  }

  public async confirm(): Promise<void> {
    try {
      await this._patientService.confirmVaccination(this.data.patientId);
      //TODO: route back to search page
      // this._router.navigate(['/admin'])
    } catch (e) {
      // this._alertService.toast(e.message);
    }
  }

}
