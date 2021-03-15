import { Component, OnInit } from '@angular/core';
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

  public personalNumber?: string;
  public patient?: Patient;

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
      }
    } catch (e) {
      this._alertService.toast(e.message);
    } finally {
      this.loading = false;
      this.submitted = false;
      this.personalNumber = '';
    }
  }

  public clearHistory(): void {
    this._searchHistoryService.clearHistory();
  }

  public searchAgain(): void {
    this.patient = undefined;
    this.personalNumber = '';
  }

  public logOut(): void {
    this._authService.logout();
  }

  public async vaccinated(): Promise<void>{
      if(this.patient){
        this._alertService.confirmVaccinateDialog(this.patient.id);
      }
  }
}
