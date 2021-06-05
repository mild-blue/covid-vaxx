import { InsuranceCompany, insuranceNumericCodeMap } from '@app/model/InsuranceCompany';
import { InsuranceCompanyDetailsDtoOut } from '@app/generated';

export const parseInsuranceCompany = (data: InsuranceCompanyDetailsDtoOut): InsuranceCompany => {
  return {
    code: data.code,
    numericCode: insuranceNumericCodeMap[data.code] ?? undefined,
    csFullName: data.csFullName,
    name: data.name
  };
};
