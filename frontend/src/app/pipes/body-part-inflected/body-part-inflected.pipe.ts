import { Pipe, PipeTransform } from '@angular/core';
import { BodyPart } from '@app/model/enums/BodyPart';

@Pipe({
  name: 'bodyPartInflected'
})
export class BodyPartInflectedPipe implements PipeTransform {

  transform(value: string): unknown {
    if (value === BodyPart.Buttock.valueOf()) {
      return 'hýždí';
    } else if (value === BodyPart.DominantHand.valueOf()) {
      return 'dominantní paže';
    } else if (value === BodyPart.NonDominantHand.valueOf()) {
      return 'nedominantní paže';
    }

    return value;
  }
}
