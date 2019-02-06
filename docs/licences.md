### Licences

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
