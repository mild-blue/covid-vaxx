import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Router } from '@angular/router';

@Component({
  selector: 'app-dialog',
  templateUrl: './success-dialog.component.html',
  styleUrls: ['./success-dialog.component.scss']
})
export class SuccessDialogComponent {

  constructor(@Inject(MAT_DIALOG_DATA) public data: { message: string; },
              private _router: Router) {
  }

  public searchPatient(): void {
    this._router.navigate(['admin']);
  }
}
