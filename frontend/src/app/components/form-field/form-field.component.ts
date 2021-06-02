import { Component, Input } from '@angular/core';
import { ValidationErrors } from '@angular/forms';

@Component({
  selector: 'app-form-field',
  templateUrl: './form-field.component.html',
  styleUrls: ['./form-field.component.scss']
})
export class FormFieldComponent {

  @Input() label?: string;
  @Input() note?: string;
  @Input() invalid: boolean = false;
  @Input() errors?: ValidationErrors | null;

  constructor() {
  }

}
