import { Component, Inject, OnInit } from '@angular/core';
import { MAT_SNACK_BAR_DATA, MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-error',
  templateUrl: './error.component.html',
  styleUrls: ['./error.component.scss']
})
export class ErrorComponent implements OnInit {
  constructor(@Inject(MAT_SNACK_BAR_DATA) public data: any,
              public snackBar: MatSnackBar) {
  }

  ngOnInit(): void {
  }

}
