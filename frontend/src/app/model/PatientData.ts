import { AnsweredQuestion } from '@app/model/AnsweredQuestion';
import { VaccinationSlotDtoOut } from '@app/generated';

export interface PatientData {
  firstName: string;
  lastName: string;
  isForeigner: boolean;
  personalNumber?: string;
  insuranceNumber?: string;
  email: string;
  phoneNumber: string;
  district: string;
  zipCode: string;
  insuranceCompany: string;
  questionnaire: AnsweredQuestion[];

  indication?: string;
  verified?: boolean;
  vaccinatedOn?: Date;
  vaccinationSlotDtoOut?: VaccinationSlotDtoOut;
  isinId?: string;
  isinReady?: boolean;

  [key: string]: undefined | unknown;
}

export const patientDataLabels: { [key: string]: string; } = {
  firstName: 'jméno',
  lastName: 'příjmení',
  personalNumber: 'rodné číslo',
  insuranceNumber: 'číslo pojištěnce',
  insuranceCompany: 'zdravotní pojišťovna',
  phoneNumber: 'telefonní číslo',
  email: 'e-mail',
  zipCode: 'PSČ',
  district: 'městská část',
  questionnaire: 'zdravotní informace'
};
