import { VaccinationSlot } from '@app/model/VaccinationSlot';
import { VaccinationLocation } from '@app/model/VaccinationLocation';

export interface RegistrationConfirmation {
  patientId?: string;
  slot: VaccinationSlot;
  location: VaccinationLocation;
}
