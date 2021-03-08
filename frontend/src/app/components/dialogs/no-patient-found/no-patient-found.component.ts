import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { NoPatientFoundInterface } from '@app/components/dialogs/no-patient-found/no-patient-found.interface';

@Component({
  selector: 'app-no-patient-found',
  templateUrl: './no-patient-found.component.html',
  styleUrls: ['./no-patient-found.component.scss']
})
export class NoPatientFoundComponent implements OnInit {

  constructor(@Inject(MAT_DIALOG_DATA) public data: NoPatientFoundInterface) {
  }

  ngOnInit(): void {
  }

}
