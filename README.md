# LITE Permissions Finder

A frontend application for providing end-user guidance on finding import and export licences.

## Getting started

* Download everything - `git clone <url>`, `cd lite-permissions-finder`, `git submodule init`, `git submodule update`
* Start a local Redis - `docker run -p 6379:6379 --name my-redis -d redis:latest`
* Copy `sample-application.conf` to `application.conf`, update `redis` details to point to your local Redis
* Run the application - `sbt run`
* Go to the index page (e.g. `http://localhost:9000`)

## Service configuration
The permissions finder integrates with several other LITE services at various stages through a journey. The services are as follows:
* [lite-country-service](https://github.com/uktrade/lite-country-service) (configured under `countryService` in config)
* [lite-control-code-service](https://github.com/uktrade/lite-control-code-service) (configured under `controlCodeService` in config)
* [lite-ogel-service](https://github.com/uktrade/lite-ogel-service) (configured under `ogelService` in config)
* [lite-ogel-registration](https://github.com/uktrade/lite-ogel-registration) (configured under `ogelRegistrationService` in config)
* [lite-notification-service](https://github.com/uktrade/lite-notification-service) (configured under `notificationService` in config)
* [lite-search-management](https://github.com/uktrade/lite-search-management) (configured under `searchService` in config)


The permissions finder also makes use of the [lite-play-common](https://github.com/uktrade/lite-play-common) base project, which 
provides various shared functionality such as base templates (`govukTemplate.template.scala`) and service clients (`CountryServiceClient`)

## Journey flow
There are two major outcomes of this application, a user is applicable for [OGEL](https://www.gov.uk/government/collections/open-general-export-licences-ogels) registration or they are not. The journey 
which makes this determination could be long and complex, but can be summarised as the following:
* The user starts off on the root path `/` and is given an "application code" to remember `ABCD-1234` which can be used to
resume their in-progress answers of this service
* The user then answers a series of questions as they see fit, and a decision is made on their suitability for an OGEL
* Should the user be applicable for an OGEL, then the permissions finder will negotiate and hand-off registration off registration of said OGEL to the [lite-ogel-registration](https://github.com/uktrade/lite-ogel-registration) application
* Guidance is shown should the user not be applicable for an OGEL (such as applying for a SIEL on [SPIRE](https://www.spire.trade.gov.uk/spire/fox/espire/LOGIN/login))

## Journey definitions
A "Journey" as defined in this project is a users track through a defined route on the system. To achieve this we have 
developed a DSL to handle navigation forward through the stages of a journey definition. See the `JourneyDefinitionBuilder`
class in [lite-play-common](https://github.com/uktrade/lite-play-common) for more information on this DSL. 

The permissions finder has two main journeys `import` and `export` which are defined in the classes 
`ImportJourneyDefinitionBuilder` and `ExportJourneyDefinitionBuilder` respectively. The current journey state is serialised 
to redis via the `PermissionsFinderDao`. This allows placing the user back to the last journey stage encountered when 
resuming an application.

## Debugging the journey
Both `import` and `export` have the ability to serialise their stages and transitions to the [Graphvis](https://en.wikipedia.org/wiki/Graphviz) 
format, this enables the generation of a visual graph of the journey. To do this, you can run the following: 
* Run the corresponding journey test in `JourneyPrint`, this should produce text in stdout along the lines of 
```text
digraph journey {
 graph [ rankdir = "LR"];
 node [shape = rectangle, fontsize=10];
 export -> exportCategory;
 ...
```
* copy this output into your favourite Graphvis rendering tool such as [WebGraphviz](http://www.webgraphviz.com/) and generate 

## Export journey in detail
See [export](docs/EXPORT.md) for details run through of the export journey

## Storage and transaction 
[Redis](https://redis.io/) is used for persistent storage and is accessed through two DAO classes, `PermissionsFinderDao` and `ApplicationCodeDao`.
* `PermissionsFinderDao` with the hash name prefix of `permissionsFinder` (if configured using the `sample-application.conf` example) is 
the primary means of storing state. All hash fields are stored against a `transactionId` which is a guid representing a user. 
The `transactionId` used by the dao is resolved from the current http context (see `TransactionIdProvider`) and forms part 
of the hash name for a field. This ID is created once the user starts a journey, and is associated with a persistent session.
There's a loosely defined naming convention when creating new fields to store which is as follows:
  * To store "myField", method `void saveMyField(String myField)` will call `writeString("myField", myfield)`
  * To retrieve "myField" method `String getMyField()` will return `readString("myField")`
  * This will result in a hash of `permissionsFinder:<some-guid>:permissionData` with a key value pair `("myField","field data")`
  * See `CommonRedisDao` for more documentation on `writeString` and `readString`
* `ApplicationCodeDao` with the hash name prefix of `applicationCodes` is a lookup table matching an application/resume code (`ABCD-1234`)
to a transactionId. Used the the "Resume my application" behavior. This results in a hash of `applicationCodes:transactionLookup:ABCD-1234` 
with a single key value pair `("transactionId","<some-guid>")`.

## Handoff to OGEL registration
After the user has selected an OGEL to register for, the permissions finder portion of the users journey is at an end and
will now be handed over to the OGEL registration application. Registration for this OGEL is carried out on the [lite-ogel-registration](https://github.com/uktrade/lite-ogel-registration) application. 
The hand over is achieved by POSTing a json payload (see `OgelRegistrationServiceRequest`) of user choices and details to the 
`/update-transaction` endpoint on the OGEL registration application.
```json
{
    "transactionId": "09a09365-b61f-4cbc-9cf6-9cc709704f4a",
    "transactionType": "OGEL_REGISTRATION",
    "transactionData": {
        "OGEL_TYPE": "OGL8"
    },
    "editSummaryFields": [
        {
            "fieldName": "Destination(s)",
            "fieldValue": "<ul><li>Alderney</li></ul>",
            "isHtml": true,
            "isValid": true,
            "editLink": "/change-destinations?ctx_journey=export~c16c7-30c54-3559d-1fce4-89ac8-2ef28-0139f-f1d6e-3f7fc-cf87b&ctx_transaction=09a09365-b61f-4cbc-9cf6-9cc709704f4a&ctx_sub_journey=search%3Aphysical&ctx_app_code=ABCD-1234"
        },
        {
            "fieldName": "Classification",
            "fieldValue": "<strong class=\\\"bold-small\\\">ML12b</strong> - Test equipment for kinetic energy projectiles and systems",
            "isHtml": true,
            "isValid": true,
            "editLink": "/change-goods-rating?ctx_journey=export~c16c7-30c54-3559d-1fce4-89ac8-2ef28-0139f-f1d6e-3f7fc-cf87b&ctx_transaction=09a09365-b61f-4cbc-9cf6-9cc709704f4a&ctx_sub_journey=search%3Aphysical&ctx_app_code=ABCD-1234"
        },
        {
            "fieldName": "Licence",
            "fieldValue": "Export after repair/replacement under warranty: military goods",
            "isHtml": false,
            "isValid": true,
            "editLink": "/change-licence-type?ctx_journey=export~c16c7-30c54-3559d-1fce4-89ac8-2ef28-0139f-f1d6e-3f7fc-cf87b&ctx_transaction=09a09365-b61f-4cbc-9cf6-9cc709704f4a&ctx_sub_journey=search%3Aphysical&ctx_app_code=ABCD-1234"
        }
    ]
}
```

Note: this endpoint requires that both permissions finder and OGEL registration have a matching shared secret, 
configurable via `ogelRegistrationService.sharedSecret` in the config, and forms part of the url 
`/update-transaction?securityToken=<some-secret>`.

The OGEL registration application will response with a json payload which should deserialise into 
`OgelRegistrationServiceResult` and will provide a redirect url if successful. The permissions finder will then redirect 
the users client to this url. See `OgelRegistrationServiceClient` for more information.
```json
{
    "status": "ok",
    "redirectUrl": "http://localhost:9090/start?transactionId=09a09365-b61f-4cbc-9cf6-9cc709704f4a"
}
```

## Resuming
Upon starting an application on the permissions finder, the user is presented with an "application code" in the format `ABCD-1234`.
This is intended to be an easy to read/write down code which will allow a user to easily "resume" an in-progress application. 
This code is stored against the `transactionId` in the `PermissionsFinderDao` and in a lookup hash `ApplicationCodeDao`, 
the format of this is detailed in an earlier section. A correctly defined applications code should resolve to a valid `transactionId` 
via the a lookup in the `ApplicationCodeDao`. From here, the users journey can be restored from the serialised journey string 
in the `PermissionsFinderDao`.

On resuming a transaction, if the key `ogelRegistrationServiceTransactionExists` in the `permissionsFinder` hash for that 
transaction is `true`. This indicates that the "resumed" application has been handed over to the OGEL registration application. 
The user will then be redirected to the OGEL registration application in the manner documented earlier. See `ContinueApplicationController` 
for more information.