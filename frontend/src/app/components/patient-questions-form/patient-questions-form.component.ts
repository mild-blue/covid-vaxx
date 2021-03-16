import { Component, Input, OnInit } from '@angular/core';
import { Patient } from '@app/model/Patient';

@Component({
  selector: 'app-patient-questions-form',
  templateUrl: './patient-questions-form.component.html',
  styleUrls: ['./patient-questions-form.component.scss']
})
export class PatientQuestionsFormComponent implements OnInit {

  @Input() patient?: Patient;

  constructor() {
  }

  ngOnInit(): void {
  }

}
