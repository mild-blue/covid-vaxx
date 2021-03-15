import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatStepperModule } from '@angular/material/stepper';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatOptionModule } from '@angular/material/core';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatDividerModule } from '@angular/material/divider';
import { MatCardModule } from '@angular/material/card';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { PatientRegisteredComponent } from './components/dialogs/patient-registered/patient-registered.component';
import { MatDialogModule } from '@angular/material/dialog';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { HomeComponent } from './pages/home/home.component';
import { ContainerComponent } from './components/container/container.component';
import { FooterComponent } from './components/footer/footer.component';
import { AdminComponent } from './pages/admin/admin.component';
import { LoginComponent } from './pages/login/login.component';
import { MAT_SNACK_BAR_DEFAULT_OPTIONS, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { ErrorInterceptor } from '@app/interceptors/error/error.interceptor';
import { AuthInterceptor } from '@app/interceptors/auth/auth.interceptor';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatBadgeModule } from '@angular/material/badge';
import { BackButtonComponent } from './components/back-button/back-button.component';
import { NoPatientFoundComponent } from './components/dialogs/no-patient-found/no-patient-found.component';
import { PatientDataComponent } from './components/patient-data/patient-data.component';
import { FormFieldComponent } from './components/form-field/form-field.component';
import { InfoComponent } from './pages/info/info.component';
import { GdprComponent } from './components/dialogs/gdpr/gdpr.component';
import { RECAPTCHA_V3_SITE_KEY, RecaptchaV3Module } from 'ng-recaptcha';
import { environment } from '@environments/environment';
import { AdminEditComponent } from './pages/admin/edit/admin-edit.component';
import { AdminSearchComponent } from './pages/admin/search/admin-search.component';
import { PatientInfoFormComponent } from './components/patient-info-form/patient-info-form.component';
import { PatientQuestionsFormComponent } from './components/patient-questions-form/patient-questions-form.component';
import { A11yModule } from '@angular/cdk/a11y';
import { AdminPatientComponent } from './pages/admin/patient/admin-patient.component';
import { AdminPatientAbstractComponent } from './pages/admin/abstract/admin-patient-abstract.component';

@NgModule({
  declarations: [
    AppComponent,
    PatientRegisteredComponent,
    HomeComponent,
    ContainerComponent,
    FooterComponent,
    AdminComponent,
    LoginComponent,
    BackButtonComponent,
    NoPatientFoundComponent,
    PatientDataComponent,
    FormFieldComponent,
    InfoComponent,
    GdprComponent,
    AdminEditComponent,
    AdminSearchComponent,
    PatientInfoFormComponent,
    PatientQuestionsFormComponent,
    AdminPatientComponent,
    AdminPatientAbstractComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MatStepperModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatToolbarModule,
    FormsModule,
    MatOptionModule,
    MatButtonToggleModule,
    MatDividerModule,
    MatCardModule,
    MatSelectModule,
    MatCheckboxModule,
    MatDialogModule,
    MatSnackBarModule,
    MatIconModule,
    MatProgressBarModule,
    MatBadgeModule,
    RecaptchaV3Module,
    A11yModule
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true },
    { provide: RECAPTCHA_V3_SITE_KEY, useValue: environment.recaptchaSiteKey },
    { provide: MAT_SNACK_BAR_DEFAULT_OPTIONS, useValue: { duration: 2500, horizontalPosition: 'start' } }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
