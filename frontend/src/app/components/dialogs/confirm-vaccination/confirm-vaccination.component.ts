import { Component, EventEmitter } from '@angular/core';
import { AbstractConfirmInterface } from '@app/components/dialogs/abstract-confirm.interface';

@Component({
  selector: 'app-confirm-vaccination',
  templateUrl: './confirm-vaccination.component.html',
  styleUrls: ['./confirm-vaccination.component.scss']
})
export class ConfirmVaccinationComponent implements AbstractConfirmInterface {

  onConfirm: EventEmitter<boolean> = new EventEmitter<boolean>();
  public isNonDominantHandUsed: boolean = false;

  confirm(): void {
    this.onConfirm.emit(this.isNonDominantHandUsed);
  }
}
