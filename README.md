# LITE Permissions Finder

A frontend application containing the following functionality:

* "Code finder" - a step-by-step wizard to help a user determine the control list entry for an item they are exporting
* "Licence finder" - allows a user to identify if any OGELs (Open General Export Licences) are applicable for their export
* OGEL registration - allows a user to register for an OGEL and view the resulting licence

## Getting started

* Download everything:
  * `git clone https://github.com/uktrade/lite-permissions-finder.git`
  * `cd lite-permissions-finder` 
  * `git submodule init`
  * `git submodule update`
* Start a local Redis: `docker run -p 6379:6379 --name my-redis -d redis:latest`
* Start a local Postgres: `docker run --name my-postgres -e POSTGRES_PASSWORD=password -p 5432:5432 -d postgres`
* Set up your local config file:
  * `cp conf/sample-application.conf conf/application.conf`
  * In service config options, replace `ENTER_USERNAME_HERE` and `ENTER_PASSWORD_HERE` values with their corresponding
    usernames and passwords from Vault.
  * If your local Redis and Postgres are not running with default options, edit the `db` and `redis` sections of the
    config file.
* Run the application: `sbt run`
* Go to the index page (e.g. `http://localhost:9000`)

## Dependency configuration

The permissions finder integrates with several other LITE services at various stages through a journey. Connection details 
(including usernames/passwords) are defined in `application.conf`.

* [lite-country-service](https://github.com/uktrade/lite-country-service) - country data for typeahead
* [lite-customer-service](https://github.com/uktrade/lite-customer-service) - company and site address information
* [lite-notification-service](https://github.com/uktrade/lite-ogel-service) (via SQS - see below) - sending emails
* [lite-ogel-service](https://github.com/uktrade/lite-ogel-service) - applicable OGEL search and OGEL details
* [lite-permissions-service](https://github.com/uktrade/lite-permissions-service) - OGEL registration submission and OGEL licence details
* [lite-user-service](https://github.com/uktrade/lite-user-service) - user account and permission data

### AWS SQS

The permissions finder communicates with the notification service by writing to an SQS queue. To test this behaviour locally,
add valid credentials to the `aws` section of `application.conf` and set `notificationService.aws.sqsQueueUrl` to the target
queue's URL.

Some notification emails are sent to a configurable inbox address - see config option `ecjuEmailAddress`.  

### Common submodule

The permissions finder also makes use of the [lite-play-common](https://github.com/uktrade/lite-play-common) base project, which 
provides various shared functionality such as base templates (`govukTemplate.template.scala`) and service clients (`CountryServiceClient`).

See [Git documentation on submodules](https://git-scm.com/book/en/v2/Git-Tools-Submodules) for details on how to manage changes
to a submodule.

### SAML IdP

The permissions finder requires a SAML Identity Provider to be configured for parts of the journey which require a user to
be authenticated. The sample configuration in `sample-application.conf` connects to a mock SAML service.

### Exporter dashboard

The permissions finder includes links to the [exporter dashboard](https://github.com/uktrade/lite-exporter-dashboard) frontend
application. Its URL is configured by the `dashboard.url` config option. 

Note: to preserve a user's authentication session between the permissions finder and the dashboard, both applications must
be configured to use the same Play secret key and serve their session cookies from a shared domain suffix.

## User journey

There is currently only one linear user journey through the application. More journeys will be added in later releases.
A user starts by identifying a control entry in the *code finder*, then proceeds to the *licence finder* if they have found one. 

### Code finder

This is a broad overview of the code finder process. Consult external design documentation for more details and business
logic.

#### 1. Start or resume an application

A user can either start a new code finder journey (internally called a session) or resume a previous one using a resume 
code. After starting a new journey, the `OnboardingController` triages them to make sure their goods are covered by the journey. 

The resume code is a randomly generated 8 character string which is stored against the session record. No authentication
is required for a session until an outcome is saved against it (therefore, a resume code may be shared between users).

See `StartApplicationController`, `ContinueApplicationController` and `OnboardingController`.

#### 2. Navigate through stages

The bulk of the user's journey involves navigating through "stages" as defined in the CMS (see below). At each stage a user
selects an answer which determines the next stage they see. Ultimately a user will arrive at one of several outcome types,
defined below.

There are three main types of stage, which behave differently:

* **Standard** - single or multiple choice between one or more answers (or "none of the above"), which may lead to another stage or an outcome.
* **Decontrol** - multiple choice of one or more answers. If any answers are selected, a "decontrol" outcome is achieved.
* **Item** - a yes/no choice in which a user confirms that a description matches their item.

User's answers are saved in the database against their session record. If a user resumes their journey, they are returned
to the last stage they were recorded on.

See `StageController`.

#### 3. Identify an outcome

The user may arrive at one of several outcome types, depending on how they respond to questions in `StageController`:

* **Item found** - if a control entry is successfully identified
* No licence required (NLR) - subtyped into:
  * **Item not found NLR** - if the user cannot identify a control entry for their item
  * **Decontrol NLR** - if the user explicitly declares their item is "decontrolled"
* **High level dropout** - if the user responds "none of the above" too soon in the journey
* **Too complex for code finder** - if an answer is flagged as too complex in the CMS

See `OutcomeController`.

#### 4. Save outcome

If the user gets an "item found" or NLR outcome, they must authenticate using the SAML IdP before continuing. 
An HTML copy of their outcome page is saved against their session record, and the session is "locked" from further 
modification (see `StageGuardAction`). The HTML is saved to provide an immutable snapshot of the information shown to the
user during their journey, regardless of subsequent content updates.
 
After an outcome is saved, only users with permissions on the customer ID or site ID saved against the outcome are able 
to view it (or government "regulator" users). The customer and site IDs to use are determined by responses from lite-customer-service
(see `CustomerServiceClient`). Note: during private beta, users must have exactly one customer and one site associated with
their account.

For an "item found" outcome, the user continues straight to the licence finder after their outcome is saved. For an NLR 
outcome, the user can generate an "NLR document". This marks the end of their journey.

See `SessionOutcomeServiceImpl`.

### Licence finder

The licence finder is a linear journey. At each stage the user is prompted for answers which are saved as fields in 
`LicenceFinderDao` (which are persisted to a Redis hash). Each stage assumes data from previous stages is available in the DAO.

#### 1. Entry from code finder

The user is directed to `EntryController` from the code finder. They must be logged in to an account with privileges for
exactly one customer and one site. The following DAO fields are set and the user is immediately redirected to `TradeController`:

* controlCode
* resumeCode
* userId
* customer
* site

#### 2. Trade type

This is a triage page to ensure the user is applying for an export licence. If they are not, they are dropped out of the
journey. The following DAO fields are set:

* tradeType
* sourceCountry (always set to UK - country code `CTRY0`)

See `TradeController`.

#### 3. Destinations

The user specifies which countries their export is going to (up to 2). The following DAO fields are set:

* destinationCountry
* multipleCountries
* firstConsigneeCountry (if multipleCountries is true)

See `DestinationsController`.

#### 4. OGEL filter questions

The user is asked further triage questions to filter the available OGELs which are shown to them on the next page. This
affects the "activity types" sent to the "applicable OGEL" OGEL service endpoint. The following DAO field is set:

* questionForm (serialised as JSON)

See `QuestionsController`.

#### 5. Choose OGEL

The user is shown a list of available OGELs based on a response from `OgelServiceClient` and chooses one.
The following DAO field is set:

* ogelId

See `ChooseOgelController`.

#### 6. Register to use OGEL

If a user selects an OGEL on the previous stage, they are shown a summary of all the answers they have given so far, 
and asked to confirm that they have fully read the OGEL conditions. This triggers the OGEL registration process detailed
below. The following DAO field is set:

* registerLicence

See `RegisterToUseController`.

#### 7. Await OGEL registration

When the user submits an OGEL registration request, the permissions finder invokes the "register OGEL" endpoint on
lite-permissions-service. This is an asynchronous process due to the time it takes to process an OGEL on SPIRE (up to a 
minute). As such, the user is shown a wait screen (`RegisterAwaitController`) while the OGEL is registered. This polls
the DAO waiting for the registrationReference field to be populated by the callback endpoint.

See `RegisterAwaitController`, `PermissionsServiceClient`.

When the permissions service completes the OGEL registration, it invokes the callback endpoint (see `RegistrationController`) 
with a payload containing the successful "registration reference". This is used as an ID for subsequently retrieving OGEL
registration information (see below).

##### 8. View OGEL registration

After the OGEL registration is complete, the user may view the details of their registration. They are prompted to do this
from the `RegisterAwaitController` following a successful registration. They are also sent an email containing a direct link.

See `ViewOgelController`.

## Implementation details

### General
 
#### Security (guard actions)

"Guard actions" are used throughout the application to prevent unauthorised access to pages in the journey using the Play
framework's action composition mechanism. E.g. `StageGuardAction` prevents a user from modifying their code finder journey
after recording an outcome.

When adding new controllers, care must be taken to ensure they are protected by an appropriate guard action.

#### Error handling

All user input (including URL parameters) should be validated in controllers. `UnknownParameterException` is thrown if
the user specifies an invalid parameter. These exceptions are mapped to a generic 404 page by `PermissionsFinderErrorHandler`.

### Code finder

#### Spreadsheet/CMS

The code finder journey is currently loaded from a spreadsheet into `UploadController`. The contents is parsed by `Loader` 
and inserted into database tables (see `cms.dao` package). More details about the loading logic are specified in 
[Confluence](https://uktrade.atlassian.net/wiki/spaces/ILT/pages/444235881/Triage+permissions+finder+technical+design).

An intermediate "config" layer sits between the database and the controller layer (see `JourneyConfigService`). This allows
the pre-parsing and local caching of "complex" fields such as `RichText`, which may contain references to other entities.
Cache loading is triggered by `CachePopulationService`. This also attempts to validate the loaded data and report on any
missing links between parsed entities.

It is envisaged that the spreadsheet load will be replaced by a dedicated set of CMS screens in a future release.

#### Persistence

The code finder uses Postgres as a permanent store of session data and journey configuration. See the Flyway migrations 
package at `db.migration.default` for the full structure.

When a new journey is loaded, all existing session answers are deleted, although sessions with outcomes saved against them
are preserved. This is because the spreadsheet loading process does not allow for persistent IDs between loads, so it is
not possible to map the previous journey stages to newly loaded stages.

### Licence finder

#### Persistence

The licence finder uses Redis as a store for session data. All data for a given session is stored in a single hash with a 
configurable TTL (config option `redis.hashTtlSeconds`). The hash's name includes the session ID.