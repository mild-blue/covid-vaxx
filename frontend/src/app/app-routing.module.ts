import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from '@app/pages/home/home.component';

const routes: Routes = [
  { path: '', pathMatch: 'full', component: HomeComponent }, // fully match an empty route

  // Redirect all to HomeComponent
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
