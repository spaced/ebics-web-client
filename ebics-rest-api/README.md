# REST API Configuration

The configuration of WAR file is externalized to have easier control over deployed application in production.
The external configuration define: 
* spring properties using application.properties (or application.yaml)
* logging settings 

##Configuration home directory

The application.properties & logback.xml is expected on path
${SPRING_CONFIG_ADDITIONAL_LOCATION}/application.properties (or application.yaml)
${SPRING_CONFIG_ADDITIONAL_LOCATION}/logback.xml

## API Key configuration
```yaml
ebics:
    api:
        enabled: true
        clients:
            FirstNameApiId:
                key: aVBeryLongRandomString
                role: admin
```
allows client using http header
```properties
X-App-Id=FirstClientApiId
X-Api-Id=aVeryLongRandomString
```

## LDAP integration

### local development
```shell
docker run --rm -p 1389:1389 --env LDAP_ADMIN_USERNAME=admin \
  --env LDAP_ADMIN_PASSWORD=adminpassword \
  --env LDAP_USERS=customuser \
  --env LDAP_PASSWORDS=custompassword bitnami/openldap:latest
```
### Configuration
#### Authentication via ldap
| configuration             | description                                                                                                                                                                                                                           |
|---------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| spring.ldap.search.domain | Authenticate with Active Directory. Typically, authentication is performed by using the domain username (in the form of user@domain), rather than using an LDAP distinguished name. Property `spring.ldap.search.domain` is required. |
| spring.ldap.username      | Authenticate with bind LDAP distinguished name|
#### Authorization via ldap
| configuration                                   | description                                                                                                                                                                     |
|-------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| spring.ldap.search.use-member-of-attribute=true | Maps attribute field `memberOf` with role using property mapping `spring.ldap.search.mapping.${groupName}=${roleNames}`                                         |
| spring.ldap.search.group.base                   | Search with `spring.ldap.search.group.base` and `spring.ldap.search.group.filter` and maps to role using property mapping `spring.ldap.search.mapping.${groupName}=${roleName}` |

### examples
openldap
```yaml
spring:
  ldap:
    enabled: true
    urls:
      - 'ldap://localhost:1389'
    base: dc=example,dc=org
    username: cn=admin,dc=example,dc=org
    password: adminpassword
    search:
      user:
        base: ou=users
        filter: (uid={0})
      group:
        base: ou=users
        filter: member={0}
      mapping:
        readers: [ 'admin' ]
```
openldap proxy to active directory
```yaml
spring:
  ldap:
    enabled: true
    urls:
      - 'ldap://localhost:1389'
    base: dc=example,dc=org
    username: cn=admin,dc=example,dc=org
    password: adminpassword
    search:
      use-member-of-attribute: true
      user:
        filter: (&(objectClass=user)(sAMAccountName={0}))
      mapping:
        readers: [ 'admin' ]
```
active directory
```yaml
spring:
  ldap:
    enabled: true
    urls: [ 'ldap://localhost:1389' ] #will pick only first url
    base: dc=example,dc=org
    search:
      domain: 'example.com'
      use-member-of-attribute: true
      mapping:
        readers: [ 'admin' ]
```

## HTTPS Certificate

In order to support HTTPS the appropriate certificate sign by verified cert authority must be configured.
For development purposes only can be used self-signed certificate

### How to create self-signed certificate for localhost (dev only)

Create private & public key:

```openssl req -x509 -new -nodes -days 720 -keyout selfsigned.key -out selfsigned.crt -config openssl.cnf```

Create PKCS12 format of keystore out of it:

```openssl pkcs12 -export -in selfsigned.crt  -inkey selfsigned.key  -out selfsigned.pfx -name ebics-rest-api```
