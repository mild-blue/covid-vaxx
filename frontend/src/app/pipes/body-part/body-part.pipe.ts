import { Pipe, PipeTransform } from '@angular/core';
import { BodyPart } from '@app/model/enums/BodyPart';

@Pipe({
  name: 'bodyPart'
})
export class BodyPartPipe implements PipeTransform {

  transform(value: string): unknown {
    if (value === BodyPart.Buttock.valueOf()) {
      return 'Hýždě';
    } else if (value === BodyPart.DominantHand.valueOf()) {
      return 'Dominantní paže';
    } else if (value === BodyPart.NonDominantHand.valueOf()) {
      return 'Nedominantní paže';
    }

    return value;
  }
}
