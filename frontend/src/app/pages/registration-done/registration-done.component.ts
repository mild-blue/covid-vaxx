import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Patient } from '@app/model/Patient';
import { PatientService } from '@app/services/patient/patient.service';
import { environment } from '@environments/environment';

@Component({
  selector: 'app-registration-done',
  templateUrl: './registration-done.component.html',
  styleUrls: ['./registration-done.component.scss']
})
export class RegistrationDoneComponent implements OnInit {

  public patientData?: Patient;
  public companyEmail: string = environment.companyEmail;

  constructor(private _router: Router,
              private _patientService: PatientService) {
  }

  ngOnInit(): void {
    this._setPatient();
  }

  private _setPatient() {
    this.patientData = this._patientService.getFromStorage();
    this._patientService.deleteFromStorage();
  }

  handleStartAgain() {
    this._router.navigate(['/registration']);
  }
}
