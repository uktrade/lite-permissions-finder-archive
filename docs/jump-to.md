## Jump To

For exporters that already know their control code, they can use the Jump To feature to skip straight through the triage and get the result they need, saving them time and effort.

### Implementation

Jump To works by filtering the database for control entries containing the search value. It then merges those results with ```Stage``` in order to get a stage ID which can be 'jumped to'. These results are then mapped to a ```ControlEntryResponse``` (more suitable for conversion to JSON) and are then returned.

Jumping to a stage ID requires a valid session ID (which the user doesn't currently have) so on ```/jump-to/go``` a new session ID is generated transparently for the user.
