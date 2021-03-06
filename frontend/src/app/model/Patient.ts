import { InsuranceCompany } from './InsuranceCompany';

export interface Patient {
  id: string;
  firstName: string;
  lastName: string;
  personalNumber: string;
  email: string;
  phoneNumber: string;
  insuranceCompany?: InsuranceCompany;
  answers: Answer[];
  created: Date;
  updated: Date;
}

export interface Answer {
  questionId: string;
  value: boolean;
}

// todo: use generated interface
export interface PatientOut {
  id: string;
  firstName: string;
  lastName: string;
  personalNumber: string;
  email: string;
  phoneNumber: string;
  insuranceCompany: string;
  answers: Answer[];
  created: number;
  updated: number;
}
