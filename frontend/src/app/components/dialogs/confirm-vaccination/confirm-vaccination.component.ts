import { Component } from '@angular/core';
import { AbstractConfirmComponent } from '@app/components/dialogs/abstract-confirm/abstract-confirm.component';

@Component({
  selector: 'app-confirm-vaccination',
  templateUrl: './confirm-vaccination.component.html',
  styleUrls: ['./confirm-vaccination.component.scss']
})
export class ConfirmVaccinationComponent extends AbstractConfirmComponent {

}
