import { EventEmitter } from '@angular/core';

export declare interface AbstractConfirmInterface {
  onConfirm: EventEmitter<any>;

  confirm(): void;
}
