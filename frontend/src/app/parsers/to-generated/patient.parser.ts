import { PatientRegistrationDtoIn, PatientUpdateDtoIn, PatientUpdateDtoInInsuranceCompanyEnum } from '../../generated';
import { Patient } from '@app/model/Patient';
import { fromQuestionToAnswerGenerated } from './question.parser';
import { InsuranceCompany } from '@app/model/InsuranceCompany';
import { fromInsuranceToInsuranceGenerated } from '@app/parsers/to-generated/insurance.parse';
import { PatientData } from '@app/model/PatientData';

export const fromPatientToRegistrationGenerated = (patient: PatientData, agreement: boolean, confirmation: boolean, gdpr: boolean): PatientRegistrationDtoIn => {
  return {
    firstName: patient.firstName.trim(),
    lastName: patient.lastName.trim(),
    personalNumber: patient.personalNumber.trim(),
    email: patient.email.trim(),
    phoneNumber: patient.phoneNumber.trim(),
    insuranceCompany: fromInsuranceToInsuranceGenerated(patient.insuranceCompany),
    answers: patient.questionnaire.map(fromQuestionToAnswerGenerated),
    confirmation: {
      covid19VaccinationAgreement: agreement,
      healthStateDisclosureConfirmation: confirmation,
      gdprAgreement: gdpr
    }
  };
};

export const fromPatientToUpdateGenerated = (patient: Patient): PatientUpdateDtoIn => {
  return {
    email: patient.email.trim(),
    firstName: patient.firstName.trim(),
    lastName: patient.lastName.trim(),
    personalNumber: patient.personalNumber.trim(),
    phoneNumber: patient.phoneNumber.trim(),
    answers: patient.questionnaire.map(fromQuestionToAnswerGenerated),
    insuranceCompany: fromInsuranceToUpdateInsuranceGenerated(patient.insuranceCompany),
    vaccinatedOn: patient.vaccinatedOn ? patient.vaccinatedOn.toISOString() : undefined
  };
};

const fromInsuranceToUpdateInsuranceGenerated = (insurance?: InsuranceCompany): PatientUpdateDtoInInsuranceCompanyEnum => {
  let converted = PatientUpdateDtoInInsuranceCompanyEnum.Cpzp;

  if (insurance) {
    const key = Object.keys(InsuranceCompany).find(v => InsuranceCompany[v as keyof typeof InsuranceCompany] === insurance);

    if (key) {
      converted = PatientUpdateDtoInInsuranceCompanyEnum[key as keyof typeof PatientUpdateDtoInInsuranceCompanyEnum];
    }
  }

  return converted;
};
