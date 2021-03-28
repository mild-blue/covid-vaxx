import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '@app/services/auth/auth.service';
import { finalize, first } from 'rxjs/operators';
import { AlertService } from '@app/services/alert/alert.service';
import { Nurse } from '@app/model/Nurse';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {

  public loginForm: FormGroup;
  public nurseForm: FormGroup;

  public loading: boolean = false;

  public email?: string;
  public nurses: Nurse[] = [];

  constructor(private _formBuilder: FormBuilder,
              private _router: Router,
              private _authService: AuthService,
              private _alertService: AlertService) {
    this.loginForm = this._formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });

    this.nurseForm = this._formBuilder.group({
      nurseId: ['', Validators.required],
      vaccine: ['', Validators.required]
    });
  }

  public cancel(): void {
    this.loginForm.reset();
    this.nurseForm.reset();
    this.nurses = [];
    this.email = '';
  }

  public getNurses(): void {

    if (this.loginForm.invalid) {
      return;
    }

    this.loading = true;
    const { username, password } = this.loginForm.controls;

    this._authService.getNurses(username.value, password.value)
    .pipe(
      first(),
      finalize(() => this.loading = false)
    )
    .subscribe(
      (nurses: Nurse[]) => {
        this.email = username.value;
        this.nurses = nurses;

        console.log('nurses', nurses);
      },
      (error: Error) => {
        this._alertService.error(error.message);
      });
  }

  public login(): void {

    console.log(this.nurseForm.value);

    if (this.nurseForm.invalid) {
      return;
    }

    this.loading = true;
    const { username, password } = this.loginForm.controls;
    const { nurseId, vaccine } = this.nurseForm.controls;

    this._authService.login(username.value, password.value, vaccine.value, nurseId.value)
    .pipe(
      first(),
      finalize(() => this.loading = false)
    )
    .subscribe(
      () => {
        this._router.navigate(['/admin']);
      },
      (error: Error) => {
        this._alertService.error(error.message);
      });
  }
}
