import { PatientDtoOutInsuranceCompanyEnum } from '@app/generated';
import { InsuranceCompany } from '@app/model/InsuranceCompany';

export const parseInsuranceCompanyFromString = (enumValue: string): InsuranceCompany | undefined => {
  const key = Object.keys(InsuranceCompany).find(v => InsuranceCompany[v as keyof typeof InsuranceCompany] === enumValue);
  return InsuranceCompany[key as keyof typeof InsuranceCompany];
};

export const parseInsuranceCompanyFromGenerated = (enumKey: PatientDtoOutInsuranceCompanyEnum): InsuranceCompany | undefined => {
  const key = Object.keys(InsuranceCompany).find(v => v === enumKey);
  return InsuranceCompany[key as keyof typeof InsuranceCompany];
};
