import { Component, EventEmitter } from '@angular/core';

@Component({ template: '' })
export class AbstractConfirmComponent {

  public onConfirm: EventEmitter<void> = new EventEmitter<void>();

  constructor() {
  }

  public async confirm(): Promise<void> {
    this.onConfirm.emit();
  }

}
