import { Component, EventEmitter } from '@angular/core';
import { AbstractConfirmInterface } from '@app/components/dialogs/abstract-confirm.interface';
import { BodyPart } from '@app/model/enums/BodyPart';

@Component({
  selector: 'app-confirm-vaccination',
  templateUrl: './confirm-vaccination.component.html',
  styleUrls: ['./confirm-vaccination.component.scss']
})
export class ConfirmVaccinationComponent implements AbstractConfirmInterface {

  onConfirm: EventEmitter<boolean> = new EventEmitter<boolean>();

  public bodyPart?: BodyPart = BodyPart.NonDominantHand;
  public bodyParts: string[] = Object.values(BodyPart);

  confirm(): void {
    console.log(this.bodyPart);
    // this.onConfirm.emit(this.isNonDominantHandUsed);
  }
}
