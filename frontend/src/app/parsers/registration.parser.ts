import { VaccinationSlotDtoOut } from '@app/generated';
import { RegistrationConfirmation } from '@app/model/RegistrationConfirmation';

export const parseVaccinationSlotToRegistrationConfirmation = (data: VaccinationSlotDtoOut): RegistrationConfirmation => {
  return {
    id: data.id,
    locationId: data.locationId,
    patientId: data.patientId ?? undefined,
    from: new Date(data.from),
    to: new Date(data.to),
    queue: data.queue
  };
};
