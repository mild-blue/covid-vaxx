import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { DialogComponent } from '@app/components/dialog/dialog.component';
import { MatDialog } from '@angular/material/dialog';

@Injectable({
  providedIn: 'root'
})
export class AlertService {

  constructor(private _snackBar: MatSnackBar,
              private _dialog: MatDialog) {
  }

  public toast(message: string): void {
    this._snackBar.open(message, 'Zavřít');
  }

  public patientRegisteredDialog(): void {
    this._dialog.open(DialogComponent, {
      width: '250px'
    });
  }
}
