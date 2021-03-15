import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from '@app/pages/home/home.component';
import { AdminComponent } from '@app/pages/admin/admin.component';
import { LoginComponent } from '@app/pages/login/login.component';
import { InfoComponent } from '@app/pages/info/info.component';
import { RegistrationDoneComponent } from '@app/pages/registration-done/registration-done.component';
import { AuthGuard } from '@app/guards/auth/auth.guard';

const routes: Routes = [
  {
    path: 'registration',
    component: HomeComponent
  },
  {
    path: 'admin',
    canActivate: [AuthGuard],
    component: AdminComponent
  },
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'info',
    component: InfoComponent
  },
  {
    path: 'registration-done',
    component: RegistrationDoneComponent 
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
