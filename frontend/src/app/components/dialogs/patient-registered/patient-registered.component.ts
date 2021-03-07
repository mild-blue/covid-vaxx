import { Component, Inject, OnInit } from '@angular/core';
import { PatientEditable } from '@app/model/PatientEditable';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-dialog',
  templateUrl: './patient-registered.component.html',
  styleUrls: ['./patient-registered.component.scss']
})
export class PatientRegisteredComponent implements OnInit {

  constructor(@Inject(MAT_DIALOG_DATA) public data: PatientEditable) {
  }

  ngOnInit(): void {
  }

}
