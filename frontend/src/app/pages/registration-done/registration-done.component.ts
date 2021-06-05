import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { PatientService } from '@app/services/patient/patient.service';
import { environment } from '@environments/environment';
import { PatientData } from '@app/model/PatientData';
import { ConfirmationService } from '@app/services/confirmation/confirmation.service';
import { RegistrationConfirmation } from '@app/model/RegistrationConfirmation';
import { defaultMapLocation, MapLocation } from '@app/model/VaccinationLocation';

@Component({
  selector: 'app-registration-done',
  templateUrl: './registration-done.component.html',
  styleUrls: ['./registration-done.component.scss']
})
export class RegistrationDoneComponent {

  public patientData?: PatientData;
  public confirmation?: RegistrationConfirmation;
  public companyEmail: string = environment.companyEmail;

  constructor(private _router: Router,
              private _confirmationService: ConfirmationService,
              private _patientService: PatientService) {
    this._patientService.patientObservable.subscribe(patient => this.patientData = patient);
    this._confirmationService.confirmationObservable.subscribe(confirmation => {
      this.confirmation = confirmation;
      this._initMap();
    });
  }

  handleStartAgain() {
    this._router.navigate(['/registration']);
  }

  private _initMap(): void {
    if (!this.confirmation?.location) {
      RegistrationDoneComponent._renderMap(defaultMapLocation);
      return;
    }

    new window.SMap.Geocoder(this.confirmation.location.address, RegistrationDoneComponent._handleGeocodingResult.bind(this));
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private static _handleGeocodingResult(geocoder: any): void {
    const results = geocoder.getResults()[0].results;
    const mapLocation = results.length ? results[0].coords : defaultMapLocation;

    RegistrationDoneComponent._renderMap(mapLocation);
  }

  private static _renderMap(location: MapLocation): void {
    // Basics
    const center = window.SMap.Coords.fromWGS84(location.x, location.y);
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
