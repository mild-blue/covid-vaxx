import { Directive } from '@angular/core';
import { AbstractControl, NG_VALIDATORS, ValidationErrors, Validator } from '@angular/forms';

@Directive({
  selector: '[postalCodeValidator]',
  providers: [{ provide: NG_VALIDATORS, useExisting: PostalCodeValidatorDirective, multi: true }]
})
export class PostalCodeValidatorDirective implements Validator {

  validate(control: AbstractControl): ValidationErrors | null {
    const regex = /^\d{3} ?\d{2}$/;
    if (!regex.test(control.value)) {
      return { postalCodeInvalid: true };
    }
    // eslint-disable-next-line no-null/no-null
    return null;
  }
}
