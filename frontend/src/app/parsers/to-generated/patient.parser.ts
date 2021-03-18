import { PatientUpdateDtoIn, PatientUpdateDtoInInsuranceCompanyEnum } from '../../generated';
import { Patient } from '@app/model/Patient';
import { fromQuestionToAnswerGenerated } from './answer.parser';
import { InsuranceCompany } from '@app/model/InsuranceCompany';

export const fromPatientToGenerated = (patient: Patient): PatientUpdateDtoIn => {
  return {
    email: patient.email.trim(),
    firstName: patient.firstName.trim(),
    lastName: patient.lastName.trim(),
    personalNumber: patient.personalNumber.trim(),
    phoneNumber: patient.phoneNumber.trim(),
    answers: patient.answers.map(fromQuestionToAnswerGenerated),
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
