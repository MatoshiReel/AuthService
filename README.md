## AuthService - user authentication and registration in the Reel app.
One of the microservices of the server-side Reel app, which provides user registration
and login using basic authentication and two-factor authentication (2FA)
via an OTP-code sent to the user's email if 2FA is enabled for this user.

The following security measures are implemented:
- Encryption of sensitive data in the request body using the AES-256-GCM algorithm;
- Hashing of user passwords and OTP-codes using the Argon2 algorithm;
- A limited number of attempts to verify the account using an OTP-code;
- Access Token signed with the RSA algorithm, returned in a response after successful registration or login;
- Data validation before saving to the database;
- Storing sensitive configuration data in an env;
- All project dependencies have been audited and cleared of vulnerabilities listed in the KEV (Known Exploited Vulnerabilities) catalog.
- This microservice operates within an internal network and is not directly exposed to external clients. 
All external communication is routed through an API Gateway, where TLS termination is enforced to ensure secure data transmission.

*** 

## Table of contents
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
  - [Internal Structure](#internal-structure)
  - [Environment Variables](#environment-variables)
  - [External Dependencies](#external-dependencies)
- [Database Schema](#database-schema)
- [Installation](#installation)
- [Usage](#usage)
- [Developer Notes](#developer-notes)
- [LICENSE](#license)

## Tech Stack

1. **Web**
    - **Spring Boot Devtools** - Microservice startup and autoconfiguration
    - **Spring Boot Web** - REST API development
2. **Security**
    - **Spring Security Crypto** - encryption and hashing
    - **Bouncy Castle**
    - **JJWT** - access token creation
    - **Spring Boot Mail** - mail sending
3. **DataBase Connection**
    - **Spring Boot Data JPA** - connection to SQL database
    - **PostgreSQL Driver**
    - **Spring Boot Data Redis** - connection to Redis
    - **Jedis**
4. **Logging**
   - **Logback** - logging framework
5. **API Documentation**
    - **SpringDoc OpenAPI** - automating generation of API documentation
6. **Testing**
    - **Spring Boot Test** - Unit testing and integration tests startup
    - **Hamcrest** - simple result assertions
    - **Rest-assured** - API testing
    - **H2** - in-memory database for testing
7. **Development Productivity**
    - **Lombok plugin** - autogeneration routine code

[Back to table of contents](#table-of-contents)

## Architecture

### Internal Structure
This microservice follows the architecture pattern CSR (Controller-Service-Repository)
with layered structure:
- **Controller** - handles HTTP requests and responses
- **Service** - contains business logic
- **Repository** - manages data persistence
- **Entity** - represents database models and used for data transfer

### Environment Variables
- **SQL Database connection**
  - **DB_DRIVER** - e.g., `org.postgresql.Driver` (PostgreSQL) or `org.h2.Driver` (H2)
  - **DB_URL** - database connection url
  - **DB_USERNAME** - database username
  - **DB_PASSWORD** - database password
- **Encryption**
  - **ENC_AES_SECRET_KEY** - AES-256 encryption key
  - **ENC_AES_SALT** - salt used for encryption
- **JWT**
  - **RSA_JWT_PRIVATE_KEY** - private key used for sign JWT
- **Mail**
  - **MAIL_USERNAME** - email address used to send messages 
  - **MAIL_PASSWORD** email app password

### External Dependencies
- **PostgreSQL** - primary data storage
- **Redis** - OTP-code storage
- **Email Service** - sending OTP-codes

[Back to table of contents](#table-of-contents)

## Database Schema

in progress

[Back to table of contents](#table-of-contents)

## Installation

in progress

[Back to table of contents](#table-of-contents)

## Usage

After starting the microservice, the API documentation is available at:
http://localhost:8081/swagger-ui.html

Or, after starting the microservice, you can download OpenAPI specification in YAML format from:
http://localhost:8081/v3/api-docs.yaml

[Back to table of contents](#table-of-contents)

## Developer Notes

If you are a developer working on this microservice, follow the guidelines below:

1. Follow a consistent code style and adhere to the "code-is-documentation" principle.
2. After adding, modifying or removing code, make sure to review and update:
   - Javadocs
   - Unit tests
3. After adding, modifying or removing code in the controller layer, make sure to review and update:
   - API documentation
   - API tests
   - Check for known vulnerabilities 
4. Commit your changes to the local git repository with a short description message for it
5. Keep the README.md file up to date and updating after code working

[Back to table of contents](#table-of-contents)

## LICENSE

This code is licensed under the GNU AGPLv3 License – see the LICENSE file for details.

[Back to table of contents](#table-of-contents)