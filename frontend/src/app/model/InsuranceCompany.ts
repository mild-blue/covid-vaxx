export interface InsuranceCompany {
  code: string;
  numericCode?: number;
  name: string;
  csFullName: string;
}

export const insuranceNumericCodeMap: { [key: string]: number; } = {
  VZP: 111,
  VOZP: 201,
  CPZP: 205,
  OZP: 207,
  ZPS: 209,
  ZPMV: 211,
  RBP: 213
};
