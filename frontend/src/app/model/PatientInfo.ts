export class PatientInfo {
  firstName?: string;
  lastName?: string;
  personalNumber?: number;
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
