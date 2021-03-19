import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'postalCode'
})
export class PostalCodePipe implements PipeTransform {

  transform(value: string): string {
    // eslint-disable-next-line no-magic-numbers
    return `${value.substring(0, 3)} ${value.substring(3, 5)}`;
  }
}
