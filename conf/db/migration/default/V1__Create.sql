CREATE TABLE journey (
  id                        BIGSERIAL PRIMARY KEY,
  timestamp                 TIMESTAMP NOT NULL DEFAULT current_timestamp,
  journey_name              TEXT      NOT NULL,
  friendly_journey_name     TEXT      NOT NULL,
  initial_stage_id          BIGINT
);
--------

CREATE TABLE control_entry (
  id                        BIGSERIAL PRIMARY KEY,
  parent_control_entry_id   BIGINT,
  control_code              TEXT      NOT NULL,
  description               TEXT      NOT NULL,
  nested                    BOOLEAN   NOT NULL,
  display_order             INT       NOT NULL DEFAULT 0,
  journey_id                BIGINT    NOT NULL,
  jump_to_control_codes     TEXT,
  decontrolled              BOOLEAN,
  FOREIGN KEY (parent_control_entry_id) REFERENCES control_entry(id),
  FOREIGN KEY (journey_id) REFERENCES journey(id)
);

CREATE INDEX fk$control_entry$parent_control_entry_id ON control_entry(parent_control_entry_id);
CREATE INDEX fk$control_entry$journey_id ON control_entry(journey_id);
--------

CREATE TABLE global_definition (
  id              BIGSERIAL PRIMARY KEY,
  journey_id      BIGINT    NOT NULL,
  term            TEXT      NOT NULL,
  definition_text TEXT      NOT NULL,
  FOREIGN KEY (journey_id) REFERENCES journey(id)
);

CREATE INDEX fk$global_definition$journey_id ON global_definition(journey_id);
--------

CREATE TABLE local_definition (
  id               BIGSERIAL PRIMARY KEY,
  control_entry_id BIGINT    NOT NULL,
  term             TEXT      NOT NULL,
  definition_text  TEXT      NOT NULL,
  FOREIGN KEY (control_entry_id) REFERENCES control_entry(id)
);

CREATE INDEX fk$local_definition$control_entry_id ON local_definition(control_entry_id);
--------

CREATE TABLE stage (
  id                 BIGSERIAL PRIMARY KEY,
  journey_id         BIGINT    NOT NULL,
  control_entry_id   BIGINT,
  title              TEXT,
  explanatory_notes  TEXT,
  question_type      TEXT      NOT NULL,
  answer_type        TEXT      NOT NULL,
  next_stage_id      BIGINT,
  go_to_outcome_type TEXT,
  FOREIGN KEY (journey_id)       REFERENCES journey(id),
  FOREIGN KEY (control_entry_id) REFERENCES control_entry(id),
  FOREIGN KEY (next_stage_id)    REFERENCES stage(id),
  CONSTRAINT question_type_value CHECK (question_type IN ('STANDARD', 'DECONTROL', 'ITEM', 'FURTHER_DECONTROL_CHECKS')),
  CONSTRAINT answer_type_value CHECK (answer_type IN ('SELECT_ONE', 'SELECT_MANY', 'PASS_THROUGH')),
  CONSTRAINT go_to_outcome_type_value CHECK (go_to_outcome_type IN ('TOO_COMPLEX')),
  CONSTRAINT stage_ck_1 CHECK (
    (question_type = 'DECONTROL' AND (next_stage_id IS NOT NULL OR go_to_outcome_type = 'TOO_COMPLEX')) OR
    (question_type != 'DECONTROL' AND go_to_outcome_type IS NULL))
);

CREATE INDEX fk$stage$journey_id ON stage(journey_id);
CREATE INDEX fk$stage$control_entry_id ON stage(control_entry_id);
CREATE INDEX fk$stage$next_stage_id ON stage(next_stage_id);
--------

CREATE TABLE note (
  id        BIGSERIAL PRIMARY KEY,
  stage_id  BIGINT    NOT NULL,
  note_text TEXT,
  note_type TEXT      NOT NULL,
  CONSTRAINT note_type_value CHECK (note_type IN ('NB', 'NOTE', 'SEE_ALSO', 'TECHNICAL_NOTE')),
  FOREIGN KEY (stage_id) REFERENCES stage(id)
);

CREATE INDEX fk$note$stage_id ON note(stage_id);
--------

CREATE TABLE stage_answer (
  id                 BIGSERIAL PRIMARY KEY,
  stage_id           BIGINT    NOT NULL,
  go_to_stage_id     BIGINT,
  go_to_outcome_type TEXT,
  control_entry_id   BIGINT,
  answer_text        TEXT,
  display_order      INT       NOT NULL,
  answer_precedence  INT,
  nested_content     TEXT,
  more_info_content  TEXT,
  FOREIGN KEY (stage_id)         REFERENCES stage(id),
  FOREIGN KEY (go_to_stage_id)   REFERENCES stage(id),
  FOREIGN KEY (control_entry_id) REFERENCES control_entry(id),
  CONSTRAINT go_to_outcome_type_value CHECK (go_to_outcome_type IN ('TOO_COMPLEX', 'DECONTROL')),
  CONSTRAINT stage_answer_ck_1 CHECK (
    (go_to_stage_id IS NULL AND go_to_outcome_type IS NOT NULL) OR
    (go_to_outcome_type IS NULL AND go_to_stage_id IS NOT NULL)),
  CONSTRAINT stage_answer_ck_2 CHECK (
    (control_entry_id IS NULL AND answer_text IS NOT NULL) OR control_entry_id IS NOT NULL)
);

CREATE INDEX fk$stage_answer$stage_id ON stage_answer(stage_id);
CREATE INDEX fk$stage_answer$go_to_stage_id ON stage_answer(go_to_stage_id);
CREATE INDEX fk$stage_answer$control_entry_id ON stage_answer(control_entry_id);
--------

CREATE TABLE spreadsheet_version (
  id 			  BIGSERIAL PRIMARY KEY,
  timestamp TIMESTAMP NOT NULL DEFAULT current_timestamp,
  filename  TEXT      NOT NULL,
  version		TEXT      NOT NULL,
  sha1		  TEXT      NOT NULL
);
--------

CREATE TABLE session (
  id                       TEXT      NOT NULL UNIQUE,
  timestamp                TIMESTAMP NOT NULL DEFAULT current_timestamp,
  journey_id               BIGINT,
  resume_code              TEXT      NOT NULL UNIQUE,
  last_stage_id 		       BIGINT,
  spreadsheet_version_id   BIGSERIAL NOT NULL,
  decontrol_codes_found    TEXT,
  control_codes_to_confirm_decontrolled_status TEXT,
  last_control_code_id     BIGINT,
  FOREIGN KEY (spreadsheet_version_id) REFERENCES spreadsheet_version(id)
);

CREATE INDEX fk$session$journey_id ON session(journey_id);
CREATE INDEX fk$session$spreadsheet_version_id ON session(spreadsheet_version_id);
--------

CREATE TABLE session_stage (
  session_id  TEXT   NOT NULL,
  stage_id    BIGINT NOT NULL,
  answer_json TEXT   NOT NULL,
  PRIMARY KEY (session_id, stage_id),
  FOREIGN KEY (session_id) REFERENCES session(id),
  FOREIGN KEY (stage_id)   REFERENCES stage(id)
);

CREATE INDEX fk$session_stage$session_id ON session_stage(session_id);
CREATE INDEX fk$session_stage$stage_id ON session_stage(stage_id);
--------

CREATE TABLE session_outcome (
  id           TEXT      NOT NULL,
  timestamp    TIMESTAMP NOT NULL DEFAULT current_timestamp,
  session_id   TEXT      NOT NULL UNIQUE,
  user_id      TEXT      NOT NULL,
  customer_id  TEXT      NOT NULL,
  site_id      TEXT      NOT NULL,
  outcome_type TEXT      NOT NULL,
  outcome_html TEXT      NOT NULL
);
--------

CREATE TABLE related_control_entry (
  control_entry_id         BIGINT NOT NULL REFERENCES control_entry(id),
  related_control_entry_id BIGINT NOT NULL REFERENCES control_entry(id),
  PRIMARY KEY(control_entry_id, related_control_entry_id)
);
--------
