import { InsuranceCompany } from '@app/model/InsuranceCompany';
import { Answer } from '@app/model/Answer';

export interface PatientData {
  firstName: string;
  lastName: string;
  personalNumber: string;
  email: string;
  phoneNumber: string;
  insuranceCompany?: InsuranceCompany;
  answers: Answer[];
  vaccinatedOn?: Date;
}
