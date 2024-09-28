# REST API Configuration

The configuration of WAR file is externalized to have easier control over deployed application in production.
The external configuration define: 
* spring properties using config.properties (or config.yaml)
* logging settings 

##Configuration home directory

The config.properties & logback.xml is expected on path
$EWC_CONFIG_HOME/config.properties (or config.yaml)
$EWC_CONFIG_HOME/logback.xml

## LDAP integration

### local development
```shell
docker run --rm -p 1389:1389 --env LDAP_ADMIN_USERNAME=admin \
  --env LDAP_ADMIN_PASSWORD=adminpassword \
  --env LDAP_USERS=customuser \
  --env LDAP_PASSWORDS=custompassword bitnami/openldap:latest
```
### Configuration
#### spring profiles for Authentication
| profile | description                                                                                                                                                                                                                           |
| --------------- |---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|ldap-bind-ad| Authenticate with Active Directory. Typically, authentication is performed by using the domain username (in the form of user@domain), rather than using an LDAP distinguished name. Property `spring.ldap.search.domain` is required. |
|ldap-bind-default| Authenticate with bind LDAP distinguished name|
#### spring profiles for Authorization
| profile               | description                                                                                                                                                                     |
|-----------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ldap-auth-ad-memberof | Maps attribute field `memberOf` with role using property mapping `spring.ldap.search.mapping.${groupName}=${roleNames}`                                         |
| ldap-auth-group-search| Search with `spring.ldap.search.group.base` and `spring.ldap.search.group.filter` and maps to role using property mapping `spring.ldap.search.mapping.${groupName}=${roleName}` |

### examples
openldap
```properties
spring.profiles.active=ldap-bind-default,ldap-auth-group-search
spring.ldap.urls=ldap://localhost:1389
spring.ldap.base=dc=example,dc=org
spring.ldap.username=cn=admin,dc=example,dc=org
spring.ldap.password=adminpassword
spring.ldap.search.user.filter=(uid={0})
spring.ldap.search.group.base=ou=users
spring.ldap.search.group.filter=member={0}
spring.ldap.search.mapping.readers=admin
```
openldap proxy to active directory
```properties
spring.profiles.active=ldap-bind-default,ldap-auth-ad-memberof
spring.ldap.urls=ldap://localhost:1389
spring.ldap.base=dc=example,dc=org
spring.ldap.username=cn=admin,dc=example,dc=org
spring.ldap.password=adminpassword
spring.ldap.search.user.filter=(&(objectClass=user)(sAMAccountName={0}))
spring.ldap.search.mapping.readers=admin
```
active directory
```properties
spring.profiles.active=ldap-bind-ad,ldap-auth-ad-memberof
spring.ldap.urls=ldap://localhost:1389
spring.ldap.base=dc=example,dc=org
spring.ldap.search.domain=example.com
spring.ldap.search.mapping.readers=admin
```

## HTTPS Certificate

In order to support HTTPS the appropriate certificate sign by verified cert authority must be configured.
For development purposes only can be used self-signed certificate

### How to create self-signed certificate for localhost (dev only)

Create private & public key:

```openssl req -x509 -new -nodes -days 720 -keyout selfsigned.key -out selfsigned.crt -config openssl.cnf```

Create PKCS12 format of keystore out of it:

```openssl pkcs12 -export -in selfsigned.crt  -inkey selfsigned.key  -out selfsigned.pfx -name ebics-rest-api```
