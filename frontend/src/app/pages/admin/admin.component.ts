import { Component, OnInit } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { validatePersonalNumber } from '@app/validators/form.validators';
import { AlertService } from '@app/services/alert/alert.service';
import { PatientService } from '@app/services/patient/patient.service';
import { Patient } from '@app/model/Patient';

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.scss']
})
export class AdminComponent implements OnInit {

  public personalNumber: FormControl = new FormControl('', [Validators.required, validatePersonalNumber]);
  public patients: Patient[] = [];

  public loading: boolean = false;
  public submitted: boolean = false;

  constructor(private _alertService: AlertService,
              private _patientService: PatientService) {
  }

  ngOnInit(): void {
  }

  get searchHistory(): string[] {
    return this._patientService.searchHistory;
  }

  public async onSubmit(): Promise<void> {
    if (this.personalNumber.invalid) {
      return;
    }

    await this.findPatient(this.personalNumber.value);
  }

  public async findPatient(personalNumber: string): Promise<void> {
    this.submitted = true;
    this.loading = true;

    try {
      this.patients = await this._patientService.findPatientByPersonalNumber(personalNumber);
      this._patientService.saveSearch(personalNumber);
    } catch (e) {
      this._alertService.toast(e.message);
    } finally {
      this.loading = false;
      this.submitted = false;
    }
  }

  public clearHistory(): void {
    this._patientService.clearHistory();
  }
}
