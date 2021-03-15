import { Directive } from '@angular/core';
import { AbstractControl, NG_VALIDATORS, ValidationErrors, Validator } from '@angular/forms';

const personalNumberAddingTwentyIssueYear = 4;
const tenDigitPersonalNumberIssueYear = 54;
const womanMonthAddition = 50;
const unprobableMonthAddition = 20;

@Directive({
  selector: '[personalNumberValidator]',
  providers: [{ provide: NG_VALIDATORS, useExisting: PersonalNumberValidatorDirective, multi: true }]
})
export class PersonalNumberValidatorDirective implements Validator {

  /**
   * Validates if date parts can form valid date.
   */
  private static _isDateValid(year: number, month: number, day: number): boolean {
    // eslint-disable-next-line no-magic-numbers
    const fullYear = year >= tenDigitPersonalNumberIssueYear ? 1900 + year : 2000 + year;
    try {
      new Date(fullYear, month, day);
      return true;
    } catch {
      return false;
    }
  }

  validate(control: AbstractControl): ValidationErrors | null {
    if (!control.value) {
      return { personalNumberInvalid: true };
    }

    let firstPart = '';
    let secondPart = '';

    const parts = control.value.split('/');
    if (parts.length === 1) {
      firstPart = control.value.substr(0, 6);
      secondPart = control.value.substr(6);
    } else {
      firstPart = parts[0];
      secondPart = parts[1];
    }

    if (firstPart.length !== 6 || isNaN(Number(firstPart)) || isNaN(Number(secondPart))) {
      return { personalNumberInvalid: true };
    }

    const year = Number(firstPart.substr(0, 2));
    let month = Number(firstPart.substr(2, 2));
    const day = Number(firstPart.substr(4, 2));

    const currentYear = (new Date()).getFullYear() % 100;

    if (year >= tenDigitPersonalNumberIssueYear || year <= currentYear) {
      if (secondPart.length === 4) {
        const controlDigit = Number(secondPart.substr(3, 1));
        const concatenated = Number(firstPart + secondPart);

        const moduloElevenOk = concatenated % 11 === 0;
        const withoutLastDigit = concatenated / 10;
        const moduloTenOk = (withoutLastDigit % 11) === 10 && controlDigit === 0;

        if (!moduloTenOk && !moduloElevenOk) {
          return { personalNumberInvalid: true };
        }
      } else {
        return { personalNumberInvalid: true };
      }
    } else {
      if (secondPart.length !== 3) {
        return { personalNumberInvalid: true };
      }
    }
    if (month > womanMonthAddition) {
      month -= womanMonthAddition;
    }

    if (month > unprobableMonthAddition) {
      if (year >= personalNumberAddingTwentyIssueYear) {
        month -= unprobableMonthAddition;
      } else {
        return { personalNumberInvalid: true };
      }
    }

    if (!PersonalNumberValidatorDirective._isDateValid(year, month, day)) {
      return { personalNumberInvalid: true };
    }
    // eslint-disable-next-line no-null/no-null
    return null;
  }
}
