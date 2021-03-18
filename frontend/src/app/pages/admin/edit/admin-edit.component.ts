import { Component, KeyValueDiffer, KeyValueDiffers, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PatientService } from '@app/services/patient/patient.service';
import { AlertService } from '@app/services/alert/alert.service';
import { AdminPatientAbstractComponent } from '@app/pages/admin/abstract/admin-patient-abstract.component';
import { Patient } from '@app/model/Patient';

@Component({
  selector: 'app-edit-patient',
  templateUrl: './admin-edit.component.html',
  styleUrls: ['./admin-edit.component.scss']
})
export class AdminEditComponent extends AdminPatientAbstractComponent implements OnInit {

  private _patientDiffer?: KeyValueDiffer<string, unknown>;
  private _patientBase?: Patient;

  public patientInfoChanged: boolean = false;

  constructor(private _route: ActivatedRoute,
              private _router: Router,
              private _patientService: PatientService,
              private _alertService: AlertService,
              private _differs: KeyValueDiffers) {
    super(_route, _patientService, _alertService);
  }

  async ngOnInit(): Promise<void> {
    await super.ngOnInit();

    if (this.patient) {
      // Create deep copy of patient
      this._patientBase = { ...this.patient };
      // Create deep copy of answers
      this._patientBase.answers = this.patient.answers.map(a => ({ ...a }));
    }

    // Listen for changes
    this._patientDiffer = this._differs.find(this.patient).create();
  }

  ngDoCheck(): void {
    if (!this.patient || !this._patientDiffer) {
      return;
    }

    const patientChanged = this._patientDiffer.diff((this.patient as unknown) as Map<string, unknown>);
    if (patientChanged) {
      // This does NOT get executed when answers differ
      // Check if info differs
      this.patientInfoChanged = this._patientInfoDiffers();
    }
  }

  private _patientInfoDiffers(): boolean {
    const old = this._patientBase;
    const current = this.patient;

    if (!old || !current) {
      return false;
    }

    return old.firstName !== current.firstName ||
      old.lastName !== current.lastName ||
      old.insuranceCompany !== current.insuranceCompany ||
      old.email !== current.email ||
      old.phoneNumber !== current.phoneNumber ||
      old.personalNumber !== current.personalNumber ||
      old.insuranceCompany !== current.insuranceCompany ||
      old.vaccinatedOn !== current.vaccinatedOn;
  }

  public answersChanged(): boolean {
    if (!this._patientBase || !this.patient) {
      return false;
    }

    const previousAnswers = this._patientBase.answers.map(a => a.value);
    const currentAnswers = this.patient.answers.map(a => a.value);

    // just to be sure
    if (previousAnswers.length !== currentAnswers.length) {
      return false;
    }

    for (let i = 0; i < previousAnswers.length; i++) {
      const previousAnswer = previousAnswers[i];
      const currentAnswer = currentAnswers[i];
      if (previousAnswer !== currentAnswer) {
        return true;
      }
    }

    return false;
  }

  public async handleSave(): Promise<void> {
    if (!this.patient) {
      return;
    }

    try {
      await this._patientService.updatePatient(this.patient);
      this._alertService.successDialog('Data pacienta byla úspěšně uložena', this._routeBack.bind(this));
    } catch (e) {
      this._alertService.error(e.message);
    }
  }

  private _routeBack(): void {
    this._router.navigate(['/admin/patient', this.id]);
  }
}
