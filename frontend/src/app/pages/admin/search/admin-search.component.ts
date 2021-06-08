import { Component } from '@angular/core';
import { Patient } from '@app/model/Patient';
import { AlertService } from '@app/services/alert/alert.service';
import { SearchHistoryService } from '@app/services/search-history/search-history.service';
import { PatientService } from '@app/services/patient/patient.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-search-patient',
  templateUrl: './admin-search.component.html',
  styleUrls: ['./admin-search.component.scss']
})
export class AdminSearchComponent {
  public isForeigner: boolean = false;
  public personalNumber?: string;
  public insuranceNumber?: string;
  public patient?: Patient;

  // We do not use getter for search history because it was not working properly in template looping
  public searchHistory: { search: string; isForeigner: boolean; }[] = this._updateSearchHistory();

  public loading: boolean = false;
  public submitted: boolean = false;

  constructor(private _alertService: AlertService,
              private _router: Router,
              private _searchHistoryService: SearchHistoryService,
              private _patientService: PatientService) {
  }

  public async onSubmit(): Promise<void> {
    if (this.isForeigner) {
      if (!this.insuranceNumber) {
        return;
      }
      await this.findPatient(undefined, this.insuranceNumber);
    } else {
      if (!this.personalNumber) {
        return;
      }
      await this.findPatient(this.personalNumber, undefined);
    }
  }

  public async findPatient(personalNumber?: string, insuranceNumber?: string): Promise<void> {
    this.submitted = true;
    this.loading = true;

    try {
      this.patient = await this._patientService.findPatientByPersonalOrInsuranceNumber(personalNumber, insuranceNumber);
      this._searchHistoryService.saveSearch(personalNumber ?? insuranceNumber ?? '', personalNumber === undefined);
      this._updateSearchHistory();
      if (!this.patient) {
        this._alertService.noPatientFoundDialog(personalNumber ?? 'aa');
      } else {
        await this._router.navigate(['/admin/patient', this.patient.id]);
      }
    } catch (e) {
      this._alertService.error(e.message);
    } finally {
      this.loading = false;
      this.submitted = false;
      this.personalNumber = '';
    }
  }

  public async findPatientForSearchHistory(query: { search: string; isForeigner: boolean; }): Promise<void> {
    if (query.isForeigner) {
      await this.findPatient(undefined, query.search);
    } else {
      await this.findPatient(query.search, undefined);
    }
  }

  public clearHistory(): void {
    this._searchHistoryService.clearHistory();
    this._updateSearchHistory();
  }

  private _updateSearchHistory(): { search: string; isForeigner: boolean; }[] {
    this.searchHistory = this._searchHistoryService.searchHistory;
    return this.searchHistory;
  }
}
