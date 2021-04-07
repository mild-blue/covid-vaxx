import { Component, Input } from '@angular/core';
import { PatientData } from '@app/model/PatientData';

@Component({
  selector: 'app-patient-data',
  templateUrl: './patient-data.component.html',
  styleUrls: ['./patient-data.component.scss']
})
export class PatientDataComponent {

  @Input() patientData?: PatientData;
  @Input() showVaccinationDate: boolean = false;

  constructor() {
  }
}
