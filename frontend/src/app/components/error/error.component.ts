import { Component, Inject } from '@angular/core';
import { MAT_SNACK_BAR_DATA, MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-error',
  templateUrl: './error.component.html',
  styleUrls: ['./error.component.scss']
})
export class ErrorComponent {

  private _timeout: any;
  private readonly _duration: number;

  constructor(@Inject(MAT_SNACK_BAR_DATA) public data: { html: string; duration: number; },
              public snackBar: MatSnackBar) {
    this._duration = data.duration;
    this.onMouseLeave();
  }

  public onMouseEnter(): void {
    if (this._timeout) {
      clearTimeout(this._timeout);
    }
  }

  public onMouseLeave(): void {
    this._timeout = setTimeout(() => this.snackBar.dismiss(), this._duration);
  }
}
