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
* [lite-notification-service](https://github.com/uktrade/lite-notification-service) (via SQS - see below) - sending emails
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

### GDS PaaS Deployment

This repo contains a pre-packed deployment file, lite-permissions-finder-xxxx.zip.  This can be used to deploy this service manually from the CF cli.  Using the following command:

* cf push [app_name] -p lite-permissions-finder-xxxx.zip

For this application to work the following dependencies need to be met:

* Bound PG DB (frontend db)
* Bound REDIS
* Env VARs will need to be set.


### Archive state

This repo is now archived: If you need to deploy this application, you can find a copy of the DB and VARs in the DIT AWS account.
