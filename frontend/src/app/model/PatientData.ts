import { InsuranceCompany } from '@app/model/InsuranceCompany';
import { AnsweredQuestion } from '@app/model/AnsweredQuestion';

export interface PatientData {
  firstName: string;
  lastName: string;
  personalNumber: string;
  email: string;
  phoneNumber: string;
  insuranceCompany?: InsuranceCompany;
  vaccinatedOn?: Date;
  questionnaire: AnsweredQuestion[];
}
