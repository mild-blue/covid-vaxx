import { Component } from '@angular/core';
import { AbstractConfirmComponent } from '@app/components/dialogs/abstract-confirm/abstract-confirm.component';

@Component({
  selector: 'app-confirm-patient-data',
  templateUrl: './confirm-patient-data.component.html',
  styleUrls: ['./confirm-patient-data.component.scss']
})
export class ConfirmPatientDataComponent extends AbstractConfirmComponent {

}
