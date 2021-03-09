import { InsuranceCompany } from '@app/model/InsuranceCompany';

export const parseInsuranceCompany = (data: string): InsuranceCompany | undefined => {
  const key = Object.keys(InsuranceCompany).find(v => InsuranceCompany[v as keyof typeof InsuranceCompany] === data);
  return InsuranceCompany[key as keyof typeof InsuranceCompany];
};
