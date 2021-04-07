import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-chip',
  templateUrl: './chip.component.html',
  styleUrls: ['./chip.component.scss']
})
export class ChipComponent {

  @Input() value: string | boolean | undefined;
  @Input() falseLabel: string = 'Ne';
  @Input() trueLabel: string = 'Ano';
  @Input() undefinedLabel: string = 'Neuvedeno';

  constructor() {
  }
}
