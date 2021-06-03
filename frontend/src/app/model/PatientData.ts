import { InsuranceCompany } from '@app/model/InsuranceCompany';
import { AnsweredQuestion } from '@app/model/AnsweredQuestion';

export interface PatientData {
  firstName: string;
  lastName: string;
  personalNumber: string;
  email: string;
  phoneNumber: string;
  district: string;
  zipCode: string;
  insuranceCompany?: InsuranceCompany;
  questionnaire: AnsweredQuestion[];

  indication?: string;
  verified?: boolean;
  vaccinatedOn?: Date;

  [key: string]: undefined | unknown;
}

export const patientDataLabels: { [key: string]: string; } = {
  firstName: 'jméno',
  lastName: 'příjmení',
  personalNumber: 'rodné číslo',
  insuranceCompany: 'zdravotní pojišťovna',
  phoneNumber: 'telefonní číslo',
  email: 'e-mail',
  zipCode: 'PSČ',
  district: 'městská část',
  questionnaire: 'zdravotní informace'
};
