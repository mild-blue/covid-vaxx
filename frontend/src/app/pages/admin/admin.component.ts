import { Component } from '@angular/core';
import { AuthService } from '@app/services/auth/auth.service';

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.scss']
})
export class AdminComponent {

  constructor(private _authService: AuthService) {
  }

  public logOut(): void {
    this._authService.logout();
  }

  public async vaccinated(): Promise<void>{
      if(this.patient){
        this._alertService.confirmVaccinateDialog(this.patient.id);
      }
  }
}
