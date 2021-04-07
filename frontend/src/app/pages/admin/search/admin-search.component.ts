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

  public personalNumber?: string;
  public patient?: Patient;

  public loading: boolean = false;
  public submitted: boolean = false;

  constructor(private _alertService: AlertService,
              private _router: Router,
              private _searchHistoryService: SearchHistoryService,
              private _patientService: PatientService) {
  }

  get searchHistory(): string[] {
    return this._searchHistoryService.searchHistory;
  }

  public async onSubmit(): Promise<void> {
    if (!this.personalNumber) {
      return;
    }

    await this.findPatient(this.personalNumber);
  }

  public async findPatient(personalNumber: string): Promise<void> {
    this.submitted = true;
    this.loading = true;

    try {
      this.patient = await this._patientService.findPatientByPersonalNumber(personalNumber);
      this._searchHistoryService.saveSearch(personalNumber);
      if (!this.patient) {
        this._alertService.noPatientFoundDialog(personalNumber);
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

  public clearHistory(): void {
    this._searchHistoryService.clearHistory();
  }
}
