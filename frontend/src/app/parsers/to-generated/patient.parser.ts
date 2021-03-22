import { AnswerDto, PatientRegistrationDtoIn, PatientUpdateDtoIn } from '../../generated';
import { fromQuestionToAnswerGenerated } from './question.parser';
import { fromInsuranceToInsuranceGenerated, fromInsuranceToUpdateInsuranceGenerated } from '@app/parsers/to-generated/insurance.parse';
import { PatientData } from '@app/model/PatientData';

export const fromPatientToRegistrationGenerated = (patient: PatientData, agreement: boolean, confirmation: boolean, gdpr: boolean): PatientRegistrationDtoIn => {
  return {
    ...fromPatientToPartialGenerated(patient),
    insuranceCompany: fromInsuranceToInsuranceGenerated(patient.insuranceCompany),
    confirmation: {
      covid19VaccinationAgreement: agreement,
      healthStateDisclosureConfirmation: confirmation,
      gdprAgreement: gdpr
    }
  };
};

export const fromPatientToUpdateGenerated = (patient: PatientData): PatientUpdateDtoIn => {
  return {
    ...fromPatientToPartialGenerated(patient),
    insuranceCompany: fromInsuranceToUpdateInsuranceGenerated(patient.insuranceCompany),
    vaccinatedOn: patient.vaccinatedOn ? patient.vaccinatedOn.toISOString() : undefined
    // TODO: Add verifiedOn
  };
};

const fromPatientToPartialGenerated = (patient: PatientData): PatientPartialDataGenerated => {
  return {
    firstName: patient.firstName.trim(),
    lastName: patient.lastName.trim(),
    personalNumber: patient.personalNumber.trim(),
    email: patient.email.trim(),
    phoneNumber: patient.phoneNumber.trim(),
    zipCode: +patient.zipCode.replace(' ', ''),
    district: patient.district.trim(),
    answers: patient.questionnaire.map(fromQuestionToAnswerGenerated)
  };
};

interface PatientPartialDataGenerated {
  firstName: string;
  lastName: string;
  personalNumber: string;
  email: string;
  phoneNumber: string;
  district: string;
  zipCode: number;
  answers: AnswerDto[];
}
