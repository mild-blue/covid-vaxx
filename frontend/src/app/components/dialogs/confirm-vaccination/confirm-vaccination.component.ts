import { Component, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-confirm-vaccination',
  templateUrl: './confirm-vaccination.component.html',
  styleUrls: ['./confirm-vaccination.component.scss']
})
export class ConfirmVaccinationComponent {

  public onConfirm: EventEmitter<void> = new EventEmitter<void>();

  constructor() {
  }

  public async confirm(): Promise<void> {
    this.onConfirm.emit();
  }
}
