import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { SuccessDialogComponent } from '@app/components/dialogs/success/success-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { NoPatientFoundComponent } from '@app/components/dialogs/no-patient-found/no-patient-found.component';
import { GdprComponent } from '@app/components/dialogs/gdpr/gdpr.component';
import { ErrorComponent } from '@app/components/error/error.component';
import { AbstractConfirmComponent } from '@app/components/dialogs/abstract-confirm/abstract-confirm.component';

@Injectable({
  providedIn: 'root'
})
export class AlertService {

  constructor(private _snackBar: MatSnackBar,
              private _dialog: MatDialog) {
  }

  public error(message: string): void {
    this._snackBar.openFromComponent(ErrorComponent, {
      duration: undefined,
      data: {
        html: message
      },
      panelClass: 'error-snack-bar'
    });
  }

  public successDialog(message: string, onClose?: () => unknown): void {
    const dialog = this._dialog.open(SuccessDialogComponent, {
      data: { message }
    });

    if (onClose) {
      dialog.afterClosed().subscribe(onClose);
    }
  }

  public noPatientFoundDialog(personalNumber: string): void {
    this._dialog.open(NoPatientFoundComponent, {
      data: { personalNumber }
    });
  }

  public gdprDialog(): void {
    this._dialog.open(GdprComponent);
  }

  public confirmDialog(component: typeof AbstractConfirmComponent, onConfirm: () => unknown): void {
    const dialog = this._dialog.open(component);
    const subscription = dialog.componentInstance.onConfirm.subscribe(onConfirm);

    dialog.afterClosed().subscribe(() => subscription.unsubscribe());
  }
}
