import { Component, OnInit } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { validatePersonalNumber } from '@app/validators/form.validators';
import { AlertService } from '@app/services/alert/alert.service';
import { PatientService } from '@app/services/patient/patient.service';
import { Patient } from '@app/model/Patient';
import { SearchHistoryService } from '@app/services/search-history/search-history.service';
import { AuthService } from '@app/services/auth/auth.service';

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
              private _authService: AuthService,
              private _searchHistoryService: SearchHistoryService,
              private _patientService: PatientService) {
  }

  ngOnInit(): void {
  }

  get searchHistory(): string[] {
    return this._searchHistoryService.searchHistory;
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
      this._searchHistoryService.saveSearch(personalNumber);
      if (!this.patients.length) {
        this._alertService.noPatientFoundDialog(personalNumber);
      }
    } catch (e) {
      this._alertService.toast(e.message);
    } finally {
      this.loading = false;
      this.submitted = false;
      this.personalNumber.reset();
    }
  }

  public clearHistory(): void {
    this._searchHistoryService.clearHistory();
  }

  public searchAgain(): void {
    this.patients = [];
    this.personalNumber.reset();
  }

  public logOut(): void {
    this._authService.logout();
  }
}
