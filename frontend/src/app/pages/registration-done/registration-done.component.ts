import { Component, OnInit } from '@angular/core';
import { QuestionService } from '@app/services/question/question.service';
import { PatientData } from '@app/model/PatientData';
import { ActivatedRoute, Router } from '@angular/router';
import { Patient } from '@app/model/Patient';
import { PatientDataComponent } from '@app/components/patient-data/patient-data.component';
import { PatientService } from '@app/services/patient/patient.service';
import { AlertService } from '@app/services/alert/alert.service';

@Component({
  selector: 'app-registration-done',
  templateUrl: './registration-done.component.html',
  styleUrls: ['./registration-done.component.scss']
})
export class RegistrationDoneComponent implements OnInit {

  public patientData: Patient;

  constructor(
    private _router: Router
  ) {
    this.patientData = {
      id: "",
      firstName: "", 
      lastName: "",
      personalNumber: "",
      email: "",
      phoneNumber: "",
      answers: [],
      created: new Date,
      updated: new Date,
    };
  }

  ngOnInit(): void {
    this._setPatient();
  }

  private _setPatient(){
    this.patientData = JSON.parse(sessionStorage.getItem("patientData") || "");
  }

  handleStartAgain() {
        sessionStorage.removeItem("patientData");
        this._router.navigate(['/registration']);
  }
}
