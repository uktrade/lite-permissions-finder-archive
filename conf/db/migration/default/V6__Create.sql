CREATE TABLE control_entry (
  id                        BIGSERIAL PRIMARY KEY,
  parent_control_entry_id   BIGINT,
  control_code              TEXT      NOT NULL,
  full_description          TEXT      NOT NULL,
  summary_description       TEXT,
  nested                    BOOLEAN   NOT NULL,
  selectable                BOOLEAN   NOT NULL,
  regime                    TEXT
);

CREATE TABLE global_definition (
  id                BIGSERIAL PRIMARY KEY,
  journey_id        BIGINT    NOT NULL,
  term              TEXT      NOT NULL,
  definition_text   TEXT      NOT NULL
);

CREATE TABLE journey (
  id                BIGSERIAL PRIMARY KEY,
  journey_name      TEXT      NOT NULL,
  initial_stage_id  BIGINT
);

CREATE TABLE local_definition (
  id                BIGSERIAL PRIMARY KEY,
  control_entry_id  BIGINT    NOT NULL,
  term              TEXT      NOT NULL,
  definition_text   TEXT      NOT NULL
);

CREATE TABLE note (
  id          BIGSERIAL PRIMARY KEY,
  stage_id    BIGINT    NOT NULL,
  note_text   TEXT,
  note_type   TEXT      NOT NULL,
  CONSTRAINT note_type_value CHECK (note_type IN ('NB', 'NOTE', 'SEE_ALSO', 'TECH_NOTE'))
);

CREATE TABLE stage (
  id                        BIGSERIAL PRIMARY KEY,
  journey_id                BIGINT    NOT NULL,
  control_entry_id          BIGINT,
  title                     TEXT,
  explanatory_notes         TEXT,
  question_type             TEXT      NOT NULL,
  answer_type               TEXT      NOT NULL,
  next_stage_id             BIGINT,
  go_to_stage_outcome_type  TEXT,
  CONSTRAINT question_type_value CHECK (question_type IN ('STANDARD', 'DECONTROL')),
  CONSTRAINT answer_type_value CHECK (answer_type IN ('SELECT_ONE', 'SELECT_MANY')),
  CONSTRAINT go_to_stage_outcome_type_value CHECK (go_to_stage_outcome_type IN ('CONTROL_ENTRY_FOUND'))
);

CREATE TABLE stage_answer (
  id                                BIGSERIAL PRIMARY KEY,
  parent_stage_id                   BIGINT    NOT NULL,
  go_to_stage_id                    BIGINT,
  go_to_stage_answer_outcome_type   TEXT,
  control_entry_id                  BIGINT,
  answer_text                       TEXT,
  display_order                     INT       NOT NULL,
  answer_precedence                 INT,
  divider_above                     BOOLEAN   NOT NULL,
  nested_content                    TEXT,
  more_info_content                 TEXT,
  CONSTRAINT go_to_stage_answer_outcome_type_value CHECK (go_to_stage_answer_outcome_type IN ('TOO_COMPLEX', 'DECONTROL', 'CONTROL_ENTRY_FOUND'))
);

ALTER TABLE control_entry
  ADD CONSTRAINT control_entry_fk_1 FOREIGN KEY (parent_control_entry_id) REFERENCES control_entry(id);

ALTER TABLE global_definition
  ADD CONSTRAINT global_definition_fk_1 FOREIGN KEY (journey_id) REFERENCES journey(id);

ALTER TABLE local_definition
  ADD CONSTRAINT local_definition_fk_1 FOREIGN KEY (control_entry_id) REFERENCES control_entry(id);

ALTER TABLE note
  ADD CONSTRAINT note_fk_1 FOREIGN KEY (stage_id) REFERENCES stage(id);

ALTER TABLE stage
  ADD CONSTRAINT stage_fk_1 FOREIGN KEY (journey_id) REFERENCES journey(id),
  ADD CONSTRAINT stage_fk_2 FOREIGN KEY (control_entry_id) REFERENCES control_entry(id),
  ADD CONSTRAINT stage_fk_3 FOREIGN KEY (next_stage_id) REFERENCES stage(id),
  ADD CONSTRAINT stage_ck_1 CHECK (
    (question_type = 'DECONTROL' AND (next_stage_id IS NOT NULL OR go_to_stage_outcome_type = 'CONTROL_ENTRY_FOUND'))
    OR (question_type != 'DECONTROL' and go_to_stage_outcome_type IS NULL)
  );

ALTER TABLE stage_answer
  ADD CONSTRAINT stage_answer_fk_1 FOREIGN KEY (parent_stage_id) REFERENCES stage(id),
  ADD CONSTRAINT stage_answer_fk_2 FOREIGN KEY (go_to_stage_id) REFERENCES stage(id),
  ADD CONSTRAINT stage_answer_fk_3 FOREIGN KEY (control_entry_id) REFERENCES control_entry(id),
  ADD CONSTRAINT stage_answer_ck_1 CHECK (
    (go_to_stage_id IS NULL AND go_to_stage_answer_outcome_type IS NOT NULL)
    OR
    (go_to_stage_answer_outcome_type IS NULL AND go_to_stage_id IS NOT NULL)
  ),
  ADD CONSTRAINT stage_answer_ck_2 CHECK (
    (control_entry_id IS NULL AND answer_text IS NOT NULL) OR control_entry_id IS NOT NULL
);

CREATE TABLE session (
  id            TEXT      NOT NULL UNIQUE,
  timestamp     TIMESTAMP NOT NULL DEFAULT current_timestamp,
  journey_id    BIGINT    NOT NULL,
  resume_code   TEXT      NOT NULL UNIQUE,
  last_stage_id BIGINT
);

CREATE TABLE session_stage (
  session_id  TEXT   NOT NULL,
  stage_id    BIGINT NOT NULL,
  answer_json TEXT   NOT NULL,
  PRIMARY KEY (session_id, stage_id),
  FOREIGN KEY (session_id) REFERENCES session(id),
  FOREIGN KEY (stage_id)   REFERENCES stage(id)
);

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
