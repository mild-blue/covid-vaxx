import { BodyPart } from './enums/BodyPart';

export interface VaccinationConfirmation {
  bodyPart: BodyPart;
  note: string;
}
