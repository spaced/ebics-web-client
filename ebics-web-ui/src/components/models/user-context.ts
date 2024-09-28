export interface UserContext {
  name: string;
  roles: Array<string>;
  time: string;
  appVersion: string;
  appBuildTimestamp: string;
}

export enum AuthenticationType {
  SSO = 'Single sign on',
  SERVER = 'server',
  HTTP_BASIC = 'HTTP Basic (username + password)'
}

export enum UserRole {
  GUEST = 'GUEST',
  USER = 'USER',
  ADMIN = 'ADMIN'
}
