/**
 * Mild Blue - Covid Vaxx
 * Covid Vaxx API
 *
 * The version of the OpenAPI document: development
 * Contact: support@mild.blue
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
import { PhoneNumberDtoIn } from './phoneNumberDtoIn';
import { AnswerDtoIn } from './answerDtoIn';
import { ConfirmationDtoIn } from './confirmationDtoIn';


export interface PatientRegistrationDtoIn {
  answers: Array<AnswerDtoIn>;
  confirmation: ConfirmationDtoIn;
  district: string;
  email: string;
  firstName: string;
  indication?: string | null;
  insuranceCompany: PatientRegistrationDtoInInsuranceCompanyEnum;
  insuranceNumber?: string | null;
  lastName: string;
  personalNumber?: string | null;
  phoneNumber: PhoneNumberDtoIn;
  zipCode: number;
}

export enum PatientRegistrationDtoInInsuranceCompanyEnum {
  Vzp = 'VZP',
  Vozp = 'VOZP',
  Cpzp = 'CPZP',
  Ozp = 'OZP',
  Zps = 'ZPS',
  Zpmv = 'ZPMV',
  Rbp = 'RBP'
};



