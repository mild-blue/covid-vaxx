import { LocationDtoOut, PatientRegistrationResponseDtoOut, VaccinationSlotDtoOut } from '@app/generated';
import { RegistrationConfirmation } from '@app/model/RegistrationConfirmation';
import { VaccinationLocation } from '@app/model/VaccinationLocation';
import { VaccinationSlot } from '@app/model/VaccinationSlot';

export const parseRegistrationResponseToRegistrationConfirmation = (data: PatientRegistrationResponseDtoOut): RegistrationConfirmation => {
  return {
    patientId: data.patientId ?? undefined,
    slot: parseSlot(data.slot),
    location: parseLocation(data.location)
  };
};

const parseSlot = (data: VaccinationSlotDtoOut): VaccinationSlot => {
  return {
    id: data.id,
    from: new Date(data.from),
    to: new Date(data.to),
    queue: data.queue
  };
};

const parseLocation = (data: LocationDtoOut): VaccinationLocation => {
  return {
    id: data.id,
    address: data.address,
    district: data.district,
    email: data.email ?? undefined,
    notes: data.notes ?? undefined,
    phoneNumber: data.phoneNumber ?? undefined,
    zipCode: `${data.zipCode}`
  };
};
