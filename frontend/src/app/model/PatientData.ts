import { InsuranceCompany } from '@app/model/InsuranceCompany';
import { AnsweredQuestion } from '@app/model/AnsweredQuestion';

export interface PatientData {
  firstName: string;
  lastName: string;
  personalNumber: string;
  email: string;
  phoneNumber: string;
  district?: string; // TODO: remove ? when BE is ready
  postalCode?: string; // TODO: remove ? when BE is ready
  insuranceCompany?: InsuranceCompany;
  vaccinatedOn?: Date;
  questionnaire: AnsweredQuestion[];
}
