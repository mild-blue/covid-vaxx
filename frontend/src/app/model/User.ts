export interface User {
  username: string;
  token: string;
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
  DOCTOR = 'DOCTOR'
}

export enum UserTokenType {
  RUP = 'RegisteredUserPrincipal',
  UP = 'UserPrincipal'
}
