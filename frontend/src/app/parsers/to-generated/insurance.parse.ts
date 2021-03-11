import { InsuranceCompany } from '@app/model/InsuranceCompany';
import { PatientRegistrationDtoInInsuranceCompanyEnum } from '@app/generated';

export const fromInsuranceToInsuranceGenerated = (insurance?: InsuranceCompany): PatientRegistrationDtoInInsuranceCompanyEnum => {
  let converted = PatientRegistrationDtoInInsuranceCompanyEnum.Cpzp;

  if (insurance) {
    const key = Object.keys(InsuranceCompany).find(v => InsuranceCompany[v as keyof typeof InsuranceCompany] === insurance);

    if (key) {
      converted = PatientRegistrationDtoInInsuranceCompanyEnum[key as keyof typeof PatientRegistrationDtoInInsuranceCompanyEnum];
    }
  }

  return converted;
};
