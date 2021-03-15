import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from '@app/pages/home/home.component';
import { AdminComponent } from '@app/pages/admin/admin.component';
import { LoginComponent } from '@app/pages/login/login.component';
import { InfoComponent } from '@app/pages/info/info.component';
import { AuthGuard } from '@app/guards/auth/auth.guard';
import { AdminEditComponent } from '@app/pages/admin/edit/admin-edit.component';
import { AdminSearchComponent } from '@app/pages/admin/search/admin-search.component';
import { AdminPatientComponent } from '@app/pages/admin/patient/admin-patient.component';

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
      { path: '', redirectTo: 'search', pathMatch: 'full' },
      { path: 'search', component: AdminSearchComponent },
      { path: 'patient/:id', component: AdminPatientComponent },
      { path: 'edit/:id', component: AdminEditComponent }
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
