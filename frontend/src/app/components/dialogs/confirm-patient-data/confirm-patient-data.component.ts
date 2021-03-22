import { Component, EventEmitter } from '@angular/core';
import { AbstractConfirmInterface } from '@app/components/dialogs/abstract-confirm.interface';

@Component({
  selector: 'app-confirm-patient-data',
  templateUrl: './confirm-patient-data.component.html',
  styleUrls: ['./confirm-patient-data.component.scss']
})
export class ConfirmPatientDataComponent implements AbstractConfirmInterface {
  onConfirm: EventEmitter<void> = new EventEmitter<void>();

  confirm(): void {
    this.onConfirm.emit();
  }
}
