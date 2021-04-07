import { PersonnelDtoOut } from '../generated';
import { Nurse } from '../model/Nurse';

export const parseNurse = (data: PersonnelDtoOut): Nurse => {
  return {
    ...data
  };
};
