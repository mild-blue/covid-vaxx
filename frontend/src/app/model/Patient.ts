import { PatientData } from '@app/model/PatientData';

export interface Patient extends PatientData {
  id: string;
  created: Date;
  updated: Date;
}
