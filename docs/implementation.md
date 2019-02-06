## Implementation

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
