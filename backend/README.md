# Covid Vaxx Backend

This project includes backend for the Mild Blue's system for vaccination registration support.

## Technology Stack

* HTTP web server - [Ktor](https://ktor.io/)
* Swagger UI - [Ktor OpenAPI Generator](https://github.com/papsign/Ktor-OpenAPI-Generator)
* SQL Framework - [Exposed](https://github.com/JetBrains/Exposed)

## Security

* We use standard implementation of authorization from Ktor.
* We use JWTs for request authentication, and we sign them with HMAC256.
* We use Role Based Authorization when we need to distinguish between roles -
  see [RoleBasedAuthorization](src/main/kotlin/blue/mild/covid/vaxx/security/auth/RoleBasedAuthorization.kt). based
  on [this article](https://www.ximedes.com/2020-09-17/role-based-authorization-in-ktor/) with modifications for OpenAPI Generator.
* We use [reCaptcha](https://developers.google.com/recaptcha/docs/v3) to verify the patients registrations.
* We have simple rate limiting built to the Ktor - [Limiter](src/main/kotlin/blue/mild/covid/vaxx/security/ddos/LinearRateLimiter.kt)
  and the [Ktor Feature](src/main/kotlin/blue/mild/covid/vaxx/security/ddos/RateLimiting.kt). Other DDoS protection is implemented in our
  reverse proxy setup in the production environment.
* We're hashing passwords using [Scrypt](https://en.wikipedia.org/wiki/Scrypt) with the [following](https://github.com/wg/scrypt)
  implementation.

## Application Configuration

See [EnvVariables](src/main/kotlin/blue/mild/covid/vaxx/setup/EnvVariables.kt) where all necessary variables are described.

## Test data

### Test user:

```json
{
  "username": "mildblue",
  "password": "bluemild",
  "role": "ADMIN"
}
```

use following SQL insert

```sql
INSERT INTO users (id, password_hash, "role", username)
VALUES ('ee99ce15-ad00-4dbc-a31d-faf732272c77', '$s0$e0801$asDyD5znh458o/+vCMIaLw==$zydsv6Cw2fKxkIGqFNFMDWQ47pKdHIInLURYOeVlYuA=', 'ADMIN',
        'mildblue');
```

### Test Patient

```json
{
  "answers": [
    {
      "questionId": "9a5587a1-dc43-49f3-9847-b736127c9e39",
      "value": "true"
    },
    {
      "questionId": "f74ebe1e-ef94-4af0-963d-97ffab086b6b",
      "value": "true"
    },
    {
      "questionId": "f68d221d-27a1-4c81-bf45-07b1f0290e15",
      "value": "false"
    },
    {
      "questionId": "f9c99047-0f44-4dfe-9964-71274a7af5e9",
      "value": "false"
    },
    {
      "questionId": "f5cf0689-a4d7-4c42-8107-6eaedca88a93",
      "value": "true"
    },
    {
      "questionId": "7b02b12a-abb4-45d3-8bf4-0b074e445f37",
      "value": "false"
    },
    {
      "questionId": "112f5fbd-cde2-4fe9-8cab-f5b4fff57296",
      "value": "true"
    },
    {
      "questionId": "f4ca8d25-faaa-4b2f-abc2-3d7a8702d4a3",
      "value": "true"
    }
  ],
  "confirmation": {
    "covid19VaccinationAgreement": true,
    "healthStateDisclosureConfirmation": true,
    "gdprAgreement": true
  },
  "firstName": "John",
  "lastName": "Doe",
  "zipCode": 16000,
  "district": "Praha 6",
  "personalNumber": "7401040020",
  "insuranceCompany": "RBP",
  "phoneNumber": "+420123456789",
  "email": "john@doe.com"
}
```

So curl looks like this:

```bash
curl -X POST "http://localhost:8080/api/patient?captcha=1234" -H "Content-Type: application/json" -d "{\"answers\":[{\"questionId\":\"9a5587a1-dc43-49f3-9847-b736127c9e39\",\"value\":\"true\"},{\"questionId\":\"f74ebe1e-ef94-4af0-963d-97ffab086b6b\",\"value\":\"true\"},{\"questionId\":\"f68d221d-27a1-4c81-bf45-07b1f0290e15\",\"value\":\"false\"},{\"questionId\":\"f9c99047-0f44-4dfe-9964-71274a7af5e9\",\"value\":\"false\"},{\"questionId\":\"f5cf0689-a4d7-4c42-8107-6eaedca88a93\",\"value\":\"true\"},{\"questionId\":\"7b02b12a-abb4-45d3-8bf4-0b074e445f37\",\"value\":\"false\"},{\"questionId\":\"112f5fbd-cde2-4fe9-8cab-f5b4fff57296\",\"value\":\"true\"},{\"questionId\":\"f4ca8d25-faaa-4b2f-abc2-3d7a8702d4a3\",\"value\":\"true\"}],\"confirmation\":{\"covid19VaccinationAgreement\":true,\"healthStateDisclosureConfirmation\":true,\"gdprAgreement\":true},\"firstName\":\"John\",\"lastName\":\"Doe\",\"zipCode\":16000,\"district\":\"Praha 6\",\"personalNumber\":\"7401040020\",\"insuranceCompany\":\"RBP\",\"phoneNumber\":\"+420123456789\",\"email\":\"john@doe.com\"}"
```
