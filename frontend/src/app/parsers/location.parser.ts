import { LocationDtoOut } from '@app/generated';
import { VaccinationLocation } from '@app/model/VaccinationLocation';

export const parseLocation = (data: LocationDtoOut): VaccinationLocation => {
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
