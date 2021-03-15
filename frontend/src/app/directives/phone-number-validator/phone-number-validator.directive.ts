import { Directive } from '@angular/core';
import { AbstractControl, NG_VALIDATORS, ValidationErrors, Validator } from '@angular/forms';

@Directive({
  selector: '[phoneNumberValidator]',
  providers: [{ provide: NG_VALIDATORS, useExisting: PhoneNumberValidatorDirective, multi: true }]
})
export class PhoneNumberValidatorDirective implements Validator {

  validate(control: AbstractControl): ValidationErrors | null {
    const regex = /^\+\d{12}$/;
    if (!regex.test(control.value)) {
      return { phoneNumberInvalid: true };
    }
    // eslint-disable-next-line no-null/no-null
    return null;
  }
}
