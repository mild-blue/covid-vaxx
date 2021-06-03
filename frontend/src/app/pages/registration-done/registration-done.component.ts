/* eslint-disable @typescript-eslint/no-explicit-any */
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { PatientService } from '@app/services/patient/patient.service';
import { environment } from '@environments/environment';
import { PatientData } from '@app/model/PatientData';
import { ConfirmationService } from '@app/services/confirmation/confirmation.service';
import { RegistrationConfirmation } from '@app/model/RegistrationConfirmation';
import { defaultLocation, defaultMapLocation, VaccinationLocation } from '@app/model/VaccinationLocation';

@Component({
  selector: 'app-registration-done',
  templateUrl: './registration-done.component.html',
  styleUrls: ['./registration-done.component.scss']
})
export class RegistrationDoneComponent implements OnInit {

  public patientData?: PatientData;
  public confirmation?: RegistrationConfirmation;
  public location?: VaccinationLocation;

  public loading: boolean = false;
  public companyEmail: string = environment.companyEmail;

  constructor(private _router: Router,
              private _confirmationService: ConfirmationService,
              private _patientService: PatientService) {
    this._patientService.patientObservable.subscribe(patient => this.patientData = patient);
    this._confirmationService.confirmationObservable.subscribe(confirmation => this.confirmation = confirmation);
  }

  async ngOnInit(): Promise<void> {
    await this._initLocation();
    this._initMap();
  }

  handleStartAgain() {
    this._router.navigate(['/registration']);
  }

  private async _initLocation() {
    if (!this.confirmation) {
      return;
    }

    this.loading = true;
    try {
      this.location = await this._confirmationService.getLocation(this.confirmation.locationId);
    } catch (e) {
      // just show default location, do not alert error
      this.location = defaultLocation;
    } finally {
      this.loading = false;
    }
  }

  private _initMap(): void {
    if (!this.location) {
      return;
    }

    new window.SMap.Geocoder(this.location.address, RegistrationDoneComponent._handleGeocodingResult.bind(this));
  }

  private static _handleGeocodingResult(geocoder: any): void {
    const results = geocoder.getResults()[0].results;
    const mapLocation = results.length ? results[0].coords : defaultMapLocation;

    // Basics
    const center = window.SMap.Coords.fromWGS84(mapLocation.x, mapLocation.y);
    const zoom = 17;

    // Marker
    const marker = new window.SMap.Marker(center, 'myMarker', {});
    const markerLayer = new window.SMap.Layer.Marker();
    markerLayer.addMarker(marker);

    // Assemble map
    const map = new window.SMap(window.JAK.gel('map'), center, zoom);

    // Add controls
    map.addControl(new window.SMap.Control.Sync());
    map.addControl(new window.SMap.Control.Compass());
    map.addControl(new window.SMap.Control.Zoom({ 2: 'Svět', 5: 'Stát', 8: 'Kraj', 11: 'Město', 14: 'Obec', 17: 'Ulice' }));
    map.addControl(new window.SMap.Control.ZoomNotification());
    map.addDefaultContextMenu();

    // Add layers
    map.addDefaultLayer(window.SMap.DEF_BASE).enable();
    map.addLayer(markerLayer).enable();
  }
}
