export class PatientInfo {
  firstName?: string;
  lastName?: string;
  personalNumber?: string;
  insuranceCompany?: string;
  phoneNumber?: string;
  email?: string;
}

export interface YesNoQuestion {
  id: string;
  label: string;
  name: string;
  value?: boolean;
}
