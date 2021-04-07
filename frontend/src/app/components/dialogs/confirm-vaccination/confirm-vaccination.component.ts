import { Component, EventEmitter, NgZone, ViewChild } from '@angular/core';
import { AbstractConfirmInterface } from '@app/components/dialogs/abstract-confirm.interface';
import { BodyPart } from '@app/model/enums/BodyPart';
import { CdkTextareaAutosize } from '@angular/cdk/text-field';
import { take } from 'rxjs/operators';
import { VaccinationConfirmation } from '@app/model/VaccinationConfirmation';

@Component({
  selector: 'app-confirm-vaccination',
  templateUrl: './confirm-vaccination.component.html',
  styleUrls: ['./confirm-vaccination.component.scss']
})
export class ConfirmVaccinationComponent implements AbstractConfirmInterface {

  @ViewChild('autosize') autosize?: CdkTextareaAutosize;
  onConfirm: EventEmitter<VaccinationConfirmation> = new EventEmitter<VaccinationConfirmation>();

  public bodyPart: BodyPart = BodyPart.NonDominantHand;
  public bodyParts: string[] = Object.values(BodyPart);

  public note: string = '';

  constructor(private _ngZone: NgZone) {
  }

  confirm(): void {
    this.onConfirm.emit({ bodyPart: this.bodyPart, note: this.note });
  }

  triggerResize() {
    if (!this.autosize) {
      return;
    }

    // Wait for changes to be applied, then trigger textarea resize.
    this._ngZone.onStable.pipe(
      take(1)
    ).subscribe(
      () => this.autosize?.resizeToFitContent(true)
    );
  }
}
