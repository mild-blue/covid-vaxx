import { PatientUpdateDtoIn, PatientUpdateDtoInInsuranceCompanyEnum } from '../../generated';
import { Patient } from '@app/model/Patient';
import { fromQuestionToAnswerGenerated } from './answer.parser';
import { InsuranceCompany } from '@app/model/InsuranceCompany';

export const fromPatientToGenerated = (patient: Patient): PatientUpdateDtoIn => {
  return {
    email: patient.email,
    firstName: patient.firstName,
    lastName: patient.lastName,
    personalNumber: patient.personalNumber,
    phoneNumber: patient.phoneNumber,
    answers: patient.answers.map(fromQuestionToAnswerGenerated),
    insuranceCompany: fromInsuranceToUpdateInsuranceGenerated(patient.insuranceCompany)
    // vaccinatedOn: string | null;  // TODO: allow to change vaccination date
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
