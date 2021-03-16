import { Component, Input } from '@angular/core';
import { Patient } from '@app/model/Patient';
import { InsuranceCompany } from '@app/model/InsuranceCompany';
import { ControlContainer, NgForm } from '@angular/forms';

@Component({
  selector: 'app-patient-info-form',
  templateUrl: './patient-info-form.component.html',
  styleUrls: ['./patient-info-form.component.scss'],
  viewProviders: [{ provide: ControlContainer, useExisting: NgForm }]
})
export class PatientInfoFormComponent {

  @Input() patient?: Patient;
  @Input() showVaccinationDate: boolean = false;

  public allInsuranceCompanies: string[] = Object.values(InsuranceCompany);
  public minVaccinationDate = new Date('1/1/2020');
  public maxVaccinationDate = new Date();
}
