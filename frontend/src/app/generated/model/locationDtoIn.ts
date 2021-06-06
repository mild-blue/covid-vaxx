/**
 * Mild Blue - Covid Vaxx
 * Covid Vaxx API
 *
 * The version of the OpenAPI document: development
 * Contact: support@mild.blue
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
import { PhoneNumberDtoIn } from './phoneNumberDtoIn';


export interface LocationDtoIn {
  address: string;
  district: string;
  email?: string | null;
  notes?: string | null;
  phoneNumber?: PhoneNumberDtoIn;
  zipCode: number;
}

