export interface User {
  username: string;
  token: string;
  role: UserRole;
  decoded: DecodedToken;
}

export interface DecodedToken {
  user_id: number;
  iat: number;
  exp: number;
  role: UserRole;
  type: UserTokenType;
}

export enum UserRole {
  ADMIN = 'ADMIN',
  DOCTOR = 'DOCTOR',
  NURSE = 'NURSE',
  RECEPTIONIST = 'RECEPTIONIST'
}

export enum UserTokenType {
  RUP = 'RegisteredUserPrincipal',
  UP = 'UserPrincipal'
}
