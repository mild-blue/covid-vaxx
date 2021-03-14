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

  public patientData?: Patient;

  constructor(
    private _router: Router,
    private _patientService: PatientService,
  ){}

  ngOnInit(): void {
    this._setPatient();
  }

  private _setPatient(){
    const savedPatient = this._patientService.getFromStorage();
    if(savedPatient){
      this.patientData = JSON.parse(savedPatient);
    }
  }

  handleStartAgain() {
        this._patientService.deleteFromStorage();
        this._router.navigate(['/registration']);
  }
}
