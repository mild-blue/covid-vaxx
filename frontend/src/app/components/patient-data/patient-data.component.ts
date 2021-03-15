import { Component, Input, OnInit } from '@angular/core';
import { PatientData } from '@app/model/PatientData';

@Component({
  selector: 'app-patient-data',
  templateUrl: './patient-data.component.html',
  styleUrls: ['./patient-data.component.scss']
})
export class PatientDataComponent implements OnInit {

  @Input() patientData?: PatientData;
  @Input() showVaccinationDate: boolean = false;

  constructor() {
  }

  ngOnInit(): void {
  }

}
