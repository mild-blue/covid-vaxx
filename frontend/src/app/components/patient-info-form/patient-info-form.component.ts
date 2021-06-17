import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { InsuranceCompany } from '@app/model/InsuranceCompany';
import { ControlContainer, FormControl, FormGroup, NgForm, ValidationErrors, Validators } from '@angular/forms';
import { PatientData, patientDataLabels } from '@app/model/PatientData';
import { InsuranceService } from '@app/services/insurance/insurance.service';

@Component({
  selector: 'app-patient-info-form',
  templateUrl: './patient-info-form.component.html',
  styleUrls: ['./patient-info-form.component.scss'],
  viewProviders: [{ provide: ControlContainer, useExisting: NgForm }]
})
export class PatientInfoFormComponent implements OnInit {

  @Input() patient?: PatientData;
  @Input() showExtraFields: boolean = false;
  @Input() vertical: boolean = false;
  @Output() missingInfo: EventEmitter<string[]> = new EventEmitter<string[]>();
  @Output() patientUpdated: EventEmitter<PatientData> = new EventEmitter<PatientData>();

  public allInsuranceCompanies: InsuranceCompany[] = [];
  public minVaccinationDate = new Date('1/1/2020');
  public maxVaccinationDate = new Date();

  public form: FormGroup = new FormGroup({
    firstName: new FormControl('', [Validators.required]),
    lastName: new FormControl('', [Validators.required]),
    isForeigner: new FormControl(false),
    personalNumber: new FormControl('', [Validators.required]),
    insuranceNumber: new FormControl('', [Validators.required]),
    insuranceCompany: new FormControl('', [Validators.required]),
    phoneNumber: new FormControl('', [Validators.required]),
    email: new FormControl('', [Validators.required]),
    zipCode: new FormControl('', [Validators.required]),
    district: new FormControl('', [Validators.required]),
    indication: new FormControl('')
  });

  constructor(private _insuranceService: InsuranceService) {
    this.form.valueChanges.subscribe(() => {
      if (!this.patient) {
        return;
      }

      // Pass invalid info to parent component
      const invalid = [];
      const controls = this.form.controls;
      const isForeigner = controls.isForeigner?.value === true;

      for (const name in controls) {
        const isPersonalNumberInvalid = name === 'personalNumber' && !isForeigner && controls[name].invalid;
        const isInsuranceNumberInvalid = name === 'insuranceNumber' && isForeigner && controls[name].invalid;
        const isOtherControlInvalid = name !== 'personalNumber' && name !== 'insuranceNumber' && controls[name].invalid;

        if (isOtherControlInvalid || isPersonalNumberInvalid || isInsuranceNumberInvalid) {
          invalid.push(patientDataLabels[name]);
        }
      }

      this.missingInfo.emit(invalid);
      this.patientUpdated.emit(this.form.value);
    });
  }

  ngOnInit() {
    if (!this.patient) {
      return;
    }

    this.form.setValue({
      firstName: this.patient.firstName,
      lastName: this.patient.lastName,
      isForeigner: this.patient.isForeigner,
      personalNumber: this.patient.personalNumber ?? null,
      insuranceNumber: this.patient.insuranceNumber ?? null,
      insuranceCompany: this.patient.insuranceCompany ?? null,
      phoneNumber: this.patient.phoneNumber,
      email: this.patient.email,
      zipCode: this.patient.zipCode,
      district: this.patient.district,
      indication: this.patient.indication ?? null
    });

    this._initInsuranceCompanies();
  }

  private async _initInsuranceCompanies(): Promise<void> {
    try {
      this.allInsuranceCompanies = await this._insuranceService.getInsuranceCompanies();
    } catch (e) {
      // ignore error
    }
  }

  public isControlInvalid(controlName: string): boolean {
    const control = this.form.controls[controlName];
    if (!control) {
      return true;
    }

    return control.invalid && (control.dirty || control.touched);
  }

  public getControlErrors(controlName: string): ValidationErrors | null {
    const control = this.form.controls[controlName];
    if (!control) {
      return null;
    }

    return control.errors;
  }
}
