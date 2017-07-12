# Export journey

The `export` journeys "objectives" are as follows: 
  * determine a control code and destination country so that we can search for OGELs, or
  * send the user to an NLR (No Licence Required) page with a reasonable degree of confidence

The following sections will run thorough a typical "best-case" export scenario. 

### Setting up the `export` journey for testing
To set up the journey in a quick "best-case" state, do the following steps:
1. On the page titled `Where are your items going? ` answer `From the UK to another country` 
to start the `export` journey.
2. On the page titled `What are you exporting?` select the `Continue` button under heading `Military goods, software and technical information`

You should now be on the page titled `Are you exporting goods, software or technical information?` 

### Physical goods, Software and Technical Information
There are three "sub-journeys" which are part to the `export` journey definition these are:
* Physical goods
* Software
* Technical information

The physical goods flow is the "simplest" of the three sub journeys and covers all key interactions with external 
services. To start this journey, select `Physical Goods` on the page tiled `Are you exporting goods, software or technical information?`

### Searching for physical goods
You should now be on page titled `Describe your item`. This page allows the user to enter terms describing their physical
good into the given input fields, which are then used as search terms, forwarded onto the `/search` endpoint of the 
[lite-search-management](https://github.com/uktrade/lite-search-management) service via the `SearchServiceClient`.

This endpoint returns a list of control codes which "match" the supplied search terms. The `GET` request may look like
`/search?term=imaging&goodsType=physical` which can return the response:
```json
{
    "results": [
        {
            "controlCode": "ML12b",
            "displayText": "Test equipment for kinetic energy projectiles and systems",
            "relatedCodes": []
        },    
        {
            "controlCode": "6A002a2a",
            "displayText": "Image intensifier tubes",
            "relatedCodes": []
        }
    ]
}
```

The two controls codes returned by the `/search` endpoint are `ML12b` and `6A002a2a`. The user would then see a list displaying
the `displayText` of this response json, which in this case would be `Testing equipment for fire control equipment for military use`
and `Image intensifier tubes`.

Picking either of these will move you through to the Decontrols, Technical Notes and Additional Specifications pages.

### Decontrols, Technical Notes and Additional Specifications
Having selected a control code from the list of search results, `6A002a2a` for example, the permissions finder will then
send a request to the [lite-control-code-service](https://github.com/uktrade/lite-control-code-service) `/frontend-control-codes` 
endpoint with the selected code as a path parameter. This endpoint will return a json object containing details on the 
control code (if found) which can map to `FrontEndControlCodeView`. 

The `GET` request may look like `/frontend-control-codes/6A002a2a` which can return the response:
```json
{
    "controlCodeData": {
        "controlCode": "6A002a2a",
        "friendlyDescription": "Image intensifier tubes with a peak response in the wavelength range between 400nm and 1,050nm, and specific electron image amplification and photocathodes",
        "title": "Image intensifier tubes",
        "technicalNotes": "<p>Charge multiplication is a form of electronic image amplification and is defined as the generation of charge carriers as a result of an impact ionization gain process. </p>\n\n<p>Charge multiplication sensors may take the form of an image intensifier tube, solid state detector or focal plane array. </p>",
        "alias": "6A002a2a",
        "additionalSpecifications": {
            "clauseText": "<p>The specific electron image amplification and photocathode specifications referred to above are:</p>",
            "specificationText": [
                {
                    "text": "Electron image amplification using either a microchannel plate with a hole pitch (centre-to-centre spacing) of 12 μm or less, or an electron sensing device with a non-binned pixel pitch of 500 μm or less, designed or modified to achieve charge multiplication other than by a microchannel plate",
                    "linkedControlCode": null
                },
                {
                    "text": "Either multialkali photocathodes with a luminous sensitivity exceeding 350 μA/lm, GaAs or GaInAs photocathodes, or other III/V compound semiconductor photocathodes with a maximum radiant sensitivity exceeding 10 mA/W",
                    "linkedControlCode": null
                }
            ]
        },
        "decontrols": [
            {
                "originControlCode": "6A002a2",
                "text": "Non-imaging photomultiplier tubes with an electron sensing device in the vacuum space limited solely either to a single metal anode or to metal anodes with a centre to centre spacing greater than 500 μm"
            }
        ]
    },
    "lineage": [
        {
            "controlCode": "6A002a2",
            "alias": "6A002a2",
            "friendlyDescription": "Image intensifier tubes and specially designed components"
        },
        {
            "controlCode": "6A002a",
            "alias": "6A002a",
            "friendlyDescription": "Optical detectors"
        },
        {
            "controlCode": "6A002",
            "alias": "6A002",
            "friendlyDescription": "Optical sensors, equipment and components"
        }
    ]
}
```

The response is fairly large, however there are a few properties of note which will influence the users journey though this section.
* `technicalNotes` - If this property is non-null, then the Technical Notes page is able to be displayed
* `additionalSpecifications` - If this property is non-null, then the Additional Specifications page is able to be displayed
* `decontrols` - If this property is non-null, then the Decontrols page is able to be displayed

The Decontrols, Technical Notes and Additional Specifications pages all display a yes/no question to the user, along side
the text in the relative property. Depending on the answer to this yes/no question, the user is either positively 
asserting that the control code does or does not apply to them. If in the affirmatives (the control code applies 
to their good) then they are moved further through the `export` journey. If in the negative (the control code does not apply) then
the user is thrown back to the list of results.

### Item destination 
After finding a control code which applies to the export good, the user will now be on a page titled 
`Where is the final destination of your items?`. This page has a type-ahead selector over a list of countries and territories.
This list is not baked into the permissions finder, but is provided by the 
[lite-country-service](https://github.com/uktrade/lite-country-service). This list is loaded at startup of the permissions 
finder and kept in a local cache, the following components provide this behavior:
* `CountryServiceClient` (in [lite-play-common](https://github.com/uktrade/lite-play-common)) requests a list countries 
from [lite-country-service](https://github.com/uktrade/lite-country-service), this can be  configured to request a particular 
group or set of countries.
* `CountryProvider` provides the list of a countries return by a configured `CountryServiceClient` and creates a cache
of said list. See `GuiceModule#provideCountryServiceExportClient`.
* `UpdateCountryCacheActor` an akka actor which facilitates the loading (and refreshing) of the cache held by a 
`CountryProvider`. See `GuiceModule#provideCountryCacheActorRefExport` and `GuiceModule#initActorScheduler`.

An example `GET` request to the [lite-country-service](https://github.com/uktrade/lite-country-service) endpoint 
`/countries/set/export-control` can return the json response:
```json

[
    {
        "countryRef": "CTRY3",
        "countryName": "Abu Dhabi"
    },
    {
        "countryRef": "CTRY4",
        "countryName": "Afghanistan"
    }
    ...
]
```

### OGEL activity types
Having selected a destination, the user should now be on the page titled `Refining your licence results` and displays
a series of yes/no questions. These questions indirectly map to an `OgelActivityType` 
(see `OgelQuestionsController#formToActivityTypes`) which are then used for a service request in the next section.
 
### Applicable OGELS
On the prior page, the user will have answered questions relating to the intended use of the physical good for export. 
The next page, titled `Your possible licences` shows the list of possible OGELs which can apply to users good. This list
is determined by a request to the [lite-ogel-service](https://github.com/uktrade/lite-ogel-service) `/applicable-ogels` 
endpoint via the `ApplicableOgelServiceClient` when provided with the following data as query parameters:
1. The control code (`controlCode`)
2. The source country (`sourceCountry`) 
3. The destination country or countries (`destinationCountry`)
4. The activity types (`activityType`)

An example `GET` request to the [lite-ogel-service](https://github.com/uktrade/lite-ogel-service) of `/applicable-ogels?controlCode=6A002a2&sourceCountry=CTRY0&activityType=DU_ANY&activityType=MIL_GOV&activityType=EXHIBITION&activityType=MIL_ANY&activityType=REPAIR&destinationCountry=CTRY3`
could return the json response:
```json
[
    {
        "id": "OGL8",
        "name": "Export after repair/replacement under warranty: military goods",
        "usageSummary": [
            "Export military items that have been temporarily in the UK for repair or replacement under warranty."
        ]
    },
    {
        "id": "OGL9",
        "name": "Export for repair/replacement under warranty: military goods",
        "usageSummary": [
            "Export previously imported military items for repair or replacement under warranty, provided they will then be returned to the UK."
        ]
    }
]
```

### Additional conditions apply
Assuming the user finds and picks an OGEL in the list shown on the prior page, the permissions finder will check 
that the `OGEL` and `Control Code` tuple has any extra conditions which could restrict it's use. To find these conditions
the permissions finder sends a request to the [lite-ogel-service](https://github.com/uktrade/lite-ogel-service) 
`/control-code-conditions` endpoint via `OgelConditionsServiceClient`. 

An example `GET` request to the [lite-ogel-service](https://github.com/uktrade/lite-ogel-service) of 
`/control-code-conditions/OGL8/ML12b` can return the json response:
```json
{
    "ogelId": "OGL8",
    "controlCode": "ML12b",
    "conditionDescription": "<p>Goods for use in connection with high-velocity gun systems capable of accelerating projectiles to 2km/s or greater</p>",
    "conditionDescriptionControlCodes": null,
    "itemsAllowed": false
}
```
An http status code of `200` (with accompanying json content) indicates the presence of additional conditions for the  
OGEL/Control Code tuple, `204` indicates no such conditions exist. The conditions are displayed to the user (should 
they exist) in the form of a yes/no question. The answer to this question determines whether this OGEL can be used in 
the scenario understood by the user.

### Virtual EU OGEL
There's a "special" OGEL which supersedes all other OGELs if applicable to the users answers so far, this is called a Virtual 
EU OGEL. This must be checked before showing the user the list of applicable OGELs described in an earlier section. 
The [lite-ogel-service](https://github.com/uktrade/lite-ogel-service) `/virtual-eu` endpoint provides this check 
(accessible via `VirtualEUOgelClient`).

An example `GET` request to the [lite-ogel-service](https://github.com/uktrade/lite-ogel-service) of 
`/virtual-eu?sourceCountry=CRTY0&destinationCountry=CRTY3&controlCode=ML1a` could return the json response:
```json
{
    "virtualEu": false
}
```

The users journey through the permission finder can here should the user be applicable for the Virtual EU OGEL,  
this is effectively a type of NLR. However if the user is not applicable for this "special" OGEL then they will progress
further through the `export` flow, on to the `Applicable OGELs` page.

### OGEL Summary
If all answers up until this point indicate that the user is applicable for the OGEL which they have selected, then a summary of
OGEL terms and conditions will be displayed. This summary is populated from data accessed via the [lite-ogel-service](https://github.com/uktrade/lite-ogel-service) 
endpoint `/ogels` with the `OGEL ID` as a path parameter (See `OgelServiceClient`).

An example `GET` request to the [lite-ogel-service](https://github.com/uktrade/lite-ogel-service) of  `/ogels/OGL1` can return the json response:
```json
{
    "id": "OGL1",
    "name": "Military goods, software and technology: government or NATO end use",
    "summary": {
        "canList": [
            "Export or transfer military items to specific governments and NATO organisations, including the re-export of items incorporated into other products in certain circumstances."
        ],
        "cantList": [
            "Export any items to be incorporated into goods, software or technology that will subsequently be exported to a non-permitted destination, unless to certain government users.",
            "Export or transfer items that perform any encryption function.",
            "Export or transfer items if you know or suspect they will be used in the proliferation of chemical, biological or nuclear weapons, either directly or indirectly.",
            "Export items intended for military use to any country under embargo imposed by the EU, OSCE or UN.",
            "Export or transfer items classified as confidential-equivalent, secret or above unless explicitly authorised by Ministry of Defence form 1686.",
            "Export or transfer items to a Customs Free Zone."
        ],
        "mustList": [
            "Export only to governments, NATO organisations, contractors with the correct documentation or armed forced deployed in authorised areas.",
            "Produce a Security Transportation Plan for exports classified as confidential or secret.",
            "Get your systems accredited if you are electronically transferring any software, data or technology classified as official-sensitive or above, and ensure the transfer is encrypted.",
            "Contact the Export Control Organisation if you have any concerns over the end use of the items.",
            "Complete a consignee undertaking for each shipment, outlining where the items are going, who the end user will be and what they will be used for.",
            "Register for this licence before your first export or transfer under the licence.",
            "Include a note stating the name of this licence or your registration reference number with all physical exports.",
            "Submit details of all export activity within each calendar year, even if you have not used the licence.",
            "Allow audits by the Export Control Organisation, and complete a pre-visit questionnaire.",
            "Comply with any conditions received in a compliance warning letter, within the timescale in the letter.",
            "Keep all documentation and records for 4 years after the date of export, including the date and destination of each export and any MOD clearance or approval letters.",
            "Use this licence at least once every 2 years, or your authorisation to use it may be withdrawn and you will have to register again."
        ],
        "howToUseList": [
            "You will need to produce a Security Transportation Plan for certain exports classified as confidential or secret.",
            "You must get government accreditation before exporting any data or technology classified as sensitive or restricted.",
            "You need to complete a consignee undertaking for each shipment you send out, listing the name and address of the end user and what they plan to use the items for.",
            "Make sure you keep all documentation and proof of your exports for at least 4 years.",
            "Your exports should all be accompanied by a note stating the name of this OGEL and/or your SPIRE reference number.",
            "You must submit a report of your exports under this licence once a year."
        ]
    },
    "link": "https://www.gov.uk/government/publications/open-general-export-licence-military-goods-government-or-nato-end-use--6"
}
```
It's now down to the user to read through the points described on this summary page (the title of which is the OGEL name).
If all has gone well, and the user agrees to the terms as set out by this summary page, then they can register for the OGEL
by clicking the button labeled `Register for this licence`.

### Answers so far, and handoff to OGEL Registration
If the user has elected to register for the OGEL suggested to them by this service, then a final summary page is displayed. 
This summary consists of the following data points, giving the user the ability to alter them prior to final submission:
* A list of export destinations
* The control code classification
* The OGEL to register for

This marks the end of the permissions finder `export` journey for `physical goods`. See the section labeled
`Handoff to OGEL registration` in the [README.md](../README.md) for more information on this handoff and interaction with 
[lite-ogel-registration](https://github.com/uktrade/lite-ogel-registration).