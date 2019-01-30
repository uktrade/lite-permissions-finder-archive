## Spreadsheet/CMS

The codefinder journey is currently loaded from a spreadsheet into `UploadController`. The contents is parsed by `Loader` 
and inserted into database tables (see `cms.dao` package). More details about the loading logic are specified in 
[Confluence](https://uktrade.atlassian.net/wiki/spaces/ILT/pages/444235881/Triage+permissions+finder+technical+design).

An intermediate "config" layer sits between the database and the controller layer (see `JourneyConfigService`). This allows
the pre-parsing and local caching of "complex" fields such as `RichText`, which may contain references to other entities.
Cache loading is triggered by `CachePopulationService`. This also attempts to validate the loaded data and report on any
missing links between parsed entities.

### Pushing changes
