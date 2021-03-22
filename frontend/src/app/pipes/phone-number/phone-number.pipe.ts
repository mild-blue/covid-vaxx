import { Pipe, PipeTransform } from '@angular/core';
import { E164Number, formatNumber, parseNumber } from 'libphonenumber-js';

@Pipe({
  name: 'phoneNumber'
})
export class PhoneNumberPipe implements PipeTransform {

  transform(value: string): E164Number | undefined {
    const parsed = parseNumber(value);

    if ('phone' in parsed) {
      return formatNumber(parsed, 'INTERNATIONAL');
    }

    return value;
  }

}
