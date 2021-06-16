import { Component, EventEmitter, NgZone, ViewChild } from '@angular/core';
import { AbstractConfirmInterface } from '@app/components/dialogs/abstract-confirm.interface';
import { CdkTextareaAutosize } from '@angular/cdk/text-field';
import { take } from 'rxjs/operators';

@Component({
  selector: 'app-confirm-patient-data',
  templateUrl: './confirm-patient-data.component.html',
  styleUrls: ['./confirm-patient-data.component.scss']
})
export class ConfirmPatientDataComponent implements AbstractConfirmInterface {

  @ViewChild('autosize') autosize?: CdkTextareaAutosize;
  onConfirm: EventEmitter<string> = new EventEmitter<string>();

  public note: string = '';
  public confirmDisabled: boolean = false;

  constructor(private _ngZone: NgZone) {
  }

  confirm(): void {
    this.confirmDisabled = true;
    this.onConfirm.emit(this.note);
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
