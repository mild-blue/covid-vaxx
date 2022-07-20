# Covid Vaxx Backend

This project includes backend for the Mild Blue's system for vaccination registration support. If you need more
information, contact [Lukas Forst](mailto:lukas@mild.blue).

## Technology Stack

* This system uses programming language [Kotlin](https://kotlinlang.org/) and [Gradle](https://gradle.org/) as build
  system.
* HTTP web server - [Ktor](https://ktor.io/).
* Swagger UI - [Ktor OpenAPI Generator](https://github.com/papsign/Ktor-OpenAPI-Generator).
* SQL Framework - [Exposed](https://github.com/JetBrains/Exposed).
* [PostgreSQL](http://postgres.com/) as our database.
* We run everything in [Docker](https://www.docker.com/) - see [Dockerfile](../Dockerfile).
* In production, we use [Fargate](https://aws.amazon.com/fargate/) and [RDS](https://aws.amazon.com/rds/).
* For development, we use [Docker-Compose](https://docs.docker.com/compose/) - [our compose file](../docker-compose.yml)
  .
* For running tests, we have another PostgreSQL instance in [docker-compose.test.yml](docker-compose.test.yml).
* We use [Detekt](https://detekt.github.io/detekt/) to keep our code consistent.
* Most of the useful commands are in [Makefile](Makefile).
* For testing we use [JUnit 5](https://junit.org/junit5/) and some other goodies
  from [Kotlin test](https://kotlinlang.org/api/latest/kotlin.test/).

## Security

* We use standard implementation of authorization from Ktor.
* We use JWTs for request authentication, and we sign them with HMAC256.
* We use Role Based Authorization when we need to distinguish between roles -
  see [RoleBasedAuthorization](src/main/kotlin/blue/mild/covid/vaxx/security/auth/RoleBasedAuthorization.kt) - based
  on [this article](https://www.ximedes.com/2020-09-17/role-based-authorization-in-ktor/) with modifications for OpenAPI
  Generator.
* We use [reCaptcha](https://developers.google.com/recaptcha/docs/v3) to verify the patients registrations.
* We have simple rate limiting built to the Ktor
  - [Limiter](src/main/kotlin/blue/mild/covid/vaxx/security/ddos/LinearRateLimiter.kt)
  and the [Ktor Feature](src/main/kotlin/blue/mild/covid/vaxx/security/ddos/RateLimiting.kt). Other DDoS protection is
  implemented in our reverse proxy setup in the production environment.
* We're hashing passwords using [Scrypt](https://en.wikipedia.org/wiki/Scrypt) with
  the [following](https://github.com/wg/scrypt)
  implementation.

## Setup

*Disclaimer: We use bash friendly operating systems, thus we know the application and all scripts work on Linux and
macOS. However, we believe that everything should work on Windows as well.*

**Note:** If you use Windows, all commands that contain `./gradlew ...` must be changed to `./gradlew.bat ...`.

**Note:** If you are using Compose V2, all commands that contain `docker-compose` must be changed to `docker compose`.
Especailly in the [Makefile](Makefile).

### Preconditions

In order to fully set up the project, one needs to have following software installed:

1. [JDK](https://adoptopenjdk.net/installation.html) - we usually use `8` or `11` to run our projects. (`11` is
   recommended)
2. [Docker](https://www.docker.com/) - in order to run the database
3. [Docker-Compose](https://docs.docker.com/compose/) - to start the database with correct configuration.
4. *(Optional)* [Make](https://www.gnu.org/software/make/) - to simply run `make` commands from [Makefile](Makefile). If
   you don't have this software installed, you can always copy-paste commands from the [Makefile](Makefile).
5. *(Optional)* [NPM](https://www.npmjs.com/get-npm) - if you want to run frontend as well. Not necessary if you want to
   just use backend.

### Running the Backend

There are basically two options how to run the server, bare-metal and docker. For development, it is usually better to
run it on bare-metal. However, for both cases you need running database - we usually use the one
in [docker-compose.yml](../docker-compose.yml). The credentials for inspecting the database are in the [.env](../.env).

Please note, that when you run the application for the first time it does not contain any data. This means that it does
not have any user in the database and for that reason you won't be able to perform most of the actions. The first user
must be created manually in the database - this is because we don't want to ship this dummy user to our production
environment. There's an SQL insert in the [Test data](#test-user)
section that you should use to create this first admin user.

```bash
docker container exec -it covid-vaxx-db-1 psql -U mildblue -d covid-vaxx
```
Then:
```sql
INSERT INTO users (first_name, last_name, email, password_hash, "role")
VALUES ('Mild', 'Blue', 'vaxx@mild.blue',
        '$s0$e0801$asDyD5znh458o/+vCMIaLw==$zydsv6Cw2fKxkIGqFNFMDWQ47pKdHIInLURYOeVlYuA=', 'ADMIN');
```


#### Bare Metal

You need to have database up & running. For that you should use included [docker-compose.yml](../docker-compose.yml).
You need to be in the directory `backend` in order for commands to work.

1. To start the database run `make db` - or `docker-compose -f ../docker-compose.yml up -d db` if you don't have Make.
2. Now when the database is running, execute `./gradlew run`
3. The application is now up & running on [localhost:8080](http://localhost:8080) *(open this address in the browser)* -
   if you installed the frontend as well, you should see the complete app (you should either set the working directory
   to `backend` or set env variable `FRONTEND_PATH` properly). If not, you can test all backend endpoints
   using [Swagger UI](https://swagger.io/)
   that is running on [localhost:8080/swagger-ui](http://localhost:8080/swagger-ui).
4. To stop the running database, run `make stop-db` or `docker-compose -f ../docker-compose.yml stop db`.

#### Docker

Again, the commands must be executed in the `backend` directory.

1. Run `make docker-run` or `docker-compose -f ../docker-compose.yml up`
2. To stop the application run `docker-compose -f ../docker-compose.yml stop`
3. When you edit some files (like code), and you want to run the application again execute `make docker-rerun` or:

```bash
docker-compose -f ../docker-compose.yml stop
docker-compose -f ../docker-compose.yml rm -f be
docker-compose -f ../docker-compose.yml up --build be
```

## Testing

In order to run all tests, you need to have the test database up & running. To do so, execute `make test-db` or
`docker-compose -f docker-compose.test.yml up -d db`.

To run all tests execute `make check` - this will start Detekt check and then all unit tests. It will also try to start
the test database. To manually run the tests execute `./gradlew check`.

### Example Tests

There are multiple base classes for the tests -
see [TestBase.kt](src/test/kotlin/blue/mild/covid/vaxx/utils/TestBase.kt) for more information.

* Classic unit testing without database and server -
  [ValidationServiceTest.kt](src/test/kotlin/blue/mild/covid/vaxx/service/ValidationServiceTest.kt) - it even has some
  nifty features like parameters testing and mocking.
* Simple integration tests that require database and starting the server
  [ServiceRoutesTest.kt](src/test/kotlin/blue/mild/covid/vaxx/routes/ServiceRoutesTest.kt).

## Application Configuration

See [EnvVariables](src/main/kotlin/blue/mild/covid/vaxx/setup/EnvVariables.kt) where all necessary variables are
described. The application uses development settings by default. So if no configuration is set, we assume the server is
running in development mode on the developer's machine -
see [ConfigurationDependencyInjection.kt](src/main/kotlin/blue/mild/covid/vaxx/setup/ConfigurationDependencyInjection.kt)
for default values.

## Test data

Following data might be useful during testing.

### Test user:

```json
{
  "email": "vaxx@mild.blue",
  "password": "bluemild",
  "role": "ADMIN"
}
```

use following SQL insert

```sql
INSERT INTO users (first_name, last_name, email, password_hash, "role")
VALUES ('Mild', 'Blue', 'vaxx@mild.blue',
        '$s0$e0801$asDyD5znh458o/+vCMIaLw==$zydsv6Cw2fKxkIGqFNFMDWQ47pKdHIInLURYOeVlYuA=', 'ADMIN');
```

### Test nurses

```sql
INSERT INTO nurses (first_name, last_name, email)
VALUES ('John', 'Doe', 'john.doe@mild.blue'),
    ('Amanda', 'Smith', 'amanda.smith@mild.blue'),
    ('Alice', 'B', 'alice.b@mild.blue');
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
  "district": "Praha 6",
  "email": "bob@doe.com",
  "firstName": "Bob",
  "indication": "Teacher",
  "insuranceCompany": "VZP",
  "lastName": "Doe",
  "personalNumber": "9109146255",
  "phoneNumber": {
    "countryCode": "CZ",
    "number": "604987321"
  },
  "zipCode": 16001
}
```

So curl looks like this:

```bash
curl -X POST "http://localhost:8080/api/patient?captcha=123456" -H "accept: application/json" -H "Content-Type: application/json" -d "{\"answers\":[{\"questionId\":\"9a5587a1-dc43-49f3-9847-b736127c9e39\",\"value\":\"true\"},{\"questionId\":\"f74ebe1e-ef94-4af0-963d-97ffab086b6b\",\"value\":\"true\"},{\"questionId\":\"f68d221d-27a1-4c81-bf45-07b1f0290e15\",\"value\":\"false\"},{\"questionId\":\"f9c99047-0f44-4dfe-9964-71274a7af5e9\",\"value\":\"false\"},{\"questionId\":\"f5cf0689-a4d7-4c42-8107-6eaedca88a93\",\"value\":\"true\"},{\"questionId\":\"7b02b12a-abb4-45d3-8bf4-0b074e445f37\",\"value\":\"false\"},{\"questionId\":\"112f5fbd-cde2-4fe9-8cab-f5b4fff57296\",\"value\":\"true\"},{\"questionId\":\"f4ca8d25-faaa-4b2f-abc2-3d7a8702d4a3\",\"value\":\"true\"}],\"confirmation\":{\"covid19VaccinationAgreement\":true,\"healthStateDisclosureConfirmation\":true,\"gdprAgreement\":true},\"district\":\"Praha 6\",\"email\":\"bob@doe.com\",\"firstName\":\"Bob\",\"indication\":\"Teacher\",\"insuranceCompany\":\"VZP\",\"lastName\":\"Doe\",\"personalNumber\":\"9109146255\",\"phoneNumber\":{\"countryCode\":\"CZ\",\"number\":\"604987321\"},\"zipCode\":16001}"
```

### Test Location

```json
{
  "address": "Markova 123",
  "district": "Praha 7",
  "email": "prh7@praha7.cz",
  "notes": "Nic moc zvlastniho",
  "phoneNumber": {
    "countryCode": "CZ",
    "number": "777123456"
  },
  "zipCode": 16001
}
```
