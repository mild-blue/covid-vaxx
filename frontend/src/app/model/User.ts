export interface User {
  email: string;
  token: string;
  decoded: DecodedToken;
}

export interface DecodedToken {
  user_id: number;
  // role: UserRole;
  iat: number;
  exp: number;
  // type: UserTokenType;
}

