import { Patient, PatientOut } from '../model/Patient';
import { InsuranceCompany } from '../model/InsuranceCompany';

export const parsePatient = (data: PatientOut): Patient => {
  return {
    ...data,
    insuranceCompany: parseInsuranceCompany(data.insuranceCompany),
    created: new Date(data.created),
    updated: new Date(data.updated)
  };
};

export const parseInsuranceCompany = (data: string): InsuranceCompany | undefined => {
  return InsuranceCompany[data as InsuranceCompany];
};
