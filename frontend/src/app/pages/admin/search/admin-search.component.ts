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

  public patient?: Patient;
  public searchQuery?: string;

  // We do not use getter for search history because it was not working properly in template looping
  public searchHistory: { search: string; }[] = this._updateSearchHistory();

  public loading: boolean = false;
  public submitted: boolean = false;

  constructor(private _alertService: AlertService,
              private _router: Router,
              private _searchHistoryService: SearchHistoryService,
              private _patientService: PatientService) {
  }

  public async onSubmit(): Promise<void> {
    await this.findPatient(this.searchQuery);
  }

  public async findPatient(searchQuery?: string): Promise<void> {
    if (!searchQuery) {
      return;
    }

    this.submitted = true;
    this.loading = true;

    try {
      this.patient = await this._patientService.findPatientByPersonalOrInsuranceNumber(searchQuery);
      this._searchHistoryService.saveSearch(searchQuery);
      this._updateSearchHistory();

      await this._router.navigate(['/admin/patient', this.patient.id]);
    } catch (e) {
      this._alertService.error(e.message);
    } finally {
      this.loading = false;
      this.submitted = false;
      this.searchQuery = '';
    }
  }

  public async findPatientForSearchHistory(query: { search: string; }): Promise<void> {
    await this.findPatient(query.search);
  }

  public clearHistory(): void {
    this._searchHistoryService.clearHistory();
    this._updateSearchHistory();
  }

  private _updateSearchHistory(): { search: string; }[] {
    this.searchHistory = this._searchHistoryService.searchHistory;
    return this.searchHistory;
  }
}
