import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from '@app/pages/home/home.component';
import { AdminComponent } from '@app/pages/admin/admin.component';

const routes: Routes = [
  { path: '', pathMatch: 'full', component: HomeComponent }, // fully match an empty route
  { path: 'admin', pathMatch: 'full', component: AdminComponent }, 

  // Redirect all to HomeComponent
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
