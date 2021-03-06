import { PatientData } from '@app/model/PatientData';

export interface Patient extends PatientData {
  id: string;
  created: Date;
  updated: Date;
}

export interface Answer {
  label: string;
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
  answers: AnswerOut[];
  created: number;
  updated: number;
}

export interface AnswerOut {
  questionId: string;
  value: boolean;
}
