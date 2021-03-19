/**
 * Mild Blue - Covid Vaxx
 * Covid Vaxx API
 *
 * The version of the OpenAPI document: 0.1.0
 * Contact: support@mild.blue
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
import { AnswerDto } from './answerDto';
import { ConfirmationDtoIn } from './confirmationDtoIn';


export interface PatientRegistrationDtoIn {
    answers: Array<AnswerDto>;
    confirmation: ConfirmationDtoIn;
    district: string;
    email: string;
    firstName: string;
    insuranceCompany: PatientRegistrationDtoInInsuranceCompanyEnum;
    lastName: string;
    personalNumber: string;
    phoneNumber: string;
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



