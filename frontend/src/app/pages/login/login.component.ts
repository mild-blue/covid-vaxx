import { Component } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '@app/services/auth/auth.service';
import { finalize, first } from 'rxjs/operators';
import { AlertService } from '@app/services/alert/alert.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {

  public loginForm: FormGroup;
  public loading: boolean = false;
  public submitted: boolean = false;

  constructor(private _formBuilder: FormBuilder,
              private _router: Router,
              private _authService: AuthService,
              private _alertService: AlertService) {
    this.loginForm = this._formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  public onSubmit(): void {

    this.submitted = true;

    if (this.loginForm.invalid) {
      return;
    }

    this.loading = true;
    const { username, password } = this.f;

    this._authService.login(username.value, password.value)
    .pipe(
      first(),
      finalize(() => this.loading = false)
    )
    .subscribe(
      () => {
        this._router.navigate(['/admin']);
      },
      (error: Error) => {
        this._alertService.toast(error.message);
      });
  }

  get f(): { [key: string]: AbstractControl; } {
    return this.loginForm.controls;
  }
}
