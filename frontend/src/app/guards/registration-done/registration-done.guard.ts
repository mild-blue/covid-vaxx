import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { PatientService } from '@app/services/patient/patient.service';

@Injectable({
  providedIn: 'root'
})
export class RegistrationDoneGuard implements CanActivate {
  constructor(private _router: Router,
              private _patientService: PatientService) {
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    return true;
    // todo: uncomment
    // if (this._patientService.patient) {
    //   // patient exists, return true
    //   return true;
    // }
    //
    // // patient does not exist, redirect to home
    // this._router.navigate(['/']);
    // return false;
  }
}
