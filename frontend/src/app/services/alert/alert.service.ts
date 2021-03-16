import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { SuccessDialogComponent } from '@app/components/dialogs/success/success-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { NoPatientFoundComponent } from '@app/components/dialogs/no-patient-found/no-patient-found.component';
import { GdprComponent } from '@app/components/dialogs/gdpr/gdpr.component';
import { ConfirmVaccinationComponent } from '@app/components/dialogs/confirm-vaccination/confirm-vaccination.component';

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

  public successDialog(message: string): void {
    this._dialog.open(SuccessDialogComponent, {
      data: { message }
    });
  }

  public noPatientFoundDialog(personalNumber: string): void {
    this._dialog.open(NoPatientFoundComponent, {
      data: { personalNumber }
    });
  }

  public gdprDialog(): void {
    this._dialog.open(GdprComponent);
  }

  public confirmVaccinateDialog(patientId: string): void{
    this._dialog.open(ConfirmVaccinationComponent, {
      data: { patientId }
    });
  }
}
