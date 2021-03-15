import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from '@app/pages/home/home.component';
import { AdminComponent } from '@app/pages/admin/admin.component';
import { LoginComponent } from '@app/pages/login/login.component';
import { InfoComponent } from '@app/pages/info/info.component';
import { AuthGuard } from '@app/guards/auth/auth.guard';
import { EditPatientComponent } from '@app/pages/edit-patient/edit-patient.component';
import { SearchPatientComponent } from '@app/pages/search-patient/search-patient.component';

const routes: Routes = [
  {
    path: 'registration',
    component: HomeComponent
  },
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'info',
    component: InfoComponent
  },

  // Admin
  {
    path: 'admin',
    canActivate: [AuthGuard],
    component: AdminComponent,
    children: [
      { path: '', component: SearchPatientComponent },
      { path: 'edit/:id', component: EditPatientComponent }
    ]
  },

  // Redirect all to InfoComponent
  { path: '', pathMatch: 'full', redirectTo: 'info' },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
