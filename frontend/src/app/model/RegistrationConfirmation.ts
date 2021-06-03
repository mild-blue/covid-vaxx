export interface RegistrationConfirmation {
  id: string;
  from: Date;
  to: Date;
  queue: number;
  locationId: string;
  patientId?: string;
}
