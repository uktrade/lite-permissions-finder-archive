## Journeys

A user starts by identifying a control entry in the *codefinder*, then proceeds to the *licence finder* if they have found one. 

### Codefinder

This is a broad overview of the codefinder process. Consult external design documentation for more details and business
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
