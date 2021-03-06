import { Component } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '@app/services/auth/auth.service';
import { finalize, first } from 'rxjs/operators';
import { User } from '@app/model/User';
import { MatSnackBar } from '@angular/material/snack-bar';

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
              private _snackBar: MatSnackBar) {
    this.loginForm = this._formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  public onSubmit() {

    this.submitted = true;

    if (this.loginForm.invalid) {
      return;
    }

    this.loading = true;
    const { email, password } = this.f;

    this._authService.login(email.value, password.value)
    .pipe(
      first(),
      finalize(() => this.loading = false)
    )
    .subscribe(
      (user: User) => {
        this._router.navigate(['/']);
      },
      (error: Error) => {
        this._snackBar.open(`<strong>Authentication error:</strong> ${error.message}`, undefined, {
          duration: 2000
        });
      });
  }

  get f(): { [key: string]: AbstractControl; } {
    return this.loginForm.controls;
  }

}
