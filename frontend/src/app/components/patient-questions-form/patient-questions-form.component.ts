import { Component, Input } from '@angular/core';
import { PatientData } from '@app/model/PatientData';

@Component({
  selector: 'app-patient-questions-form',
  templateUrl: './patient-questions-form.component.html',
  styleUrls: ['./patient-questions-form.component.scss']
})
export class PatientQuestionsFormComponent {

  @Input() patient?: PatientData;

  constructor() {
  }

}
