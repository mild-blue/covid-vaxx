import { Injectable } from '@angular/core';
import { InsuranceCompanyDetailsDtoOut } from '@app/generated';
import { environment } from '@environments/environment';
import { first, map } from 'rxjs/operators';
import { InsuranceCompany } from '@app/model/InsuranceCompany';
import { HttpClient } from '@angular/common/http';
import { parseInsuranceCompany } from '@app/parsers/insurance.parser';

@Injectable({
  providedIn: 'root'
})
export class InsuranceService {

  constructor(private _http: HttpClient) {
  }

  public async getInsuranceCompanies(): Promise<InsuranceCompany[]> {
    return this._http.get<InsuranceCompanyDetailsDtoOut[]>(
      `${environment.apiUrl}/insurance-companies`
    ).pipe(
      first(),
      map(companies => companies.map(parseInsuranceCompany))
    ).toPromise();
  }
}
