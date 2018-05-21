-- Types
CREATE TYPE answer_type AS ENUM ('SELECT_ONE', 'SELECT_MANY');

CREATE TYPE stage_outcome_type AS ENUM ('CONTROL_ENTRY_FOUND');

CREATE TYPE stage_answer_outcome_type AS ENUM ('TOO_COMPLEX', 'DECONTROL', 'CONTROL_ENTRY_FOUND');

CREATE TYPE note_type AS ENUM ('NB', 'NOTE', 'SEE_ALSO', 'TECH_NOTE');

CREATE TYPE question_type AS ENUM ('STANDARD', 'DECONTROL');

-- Tables
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
  id            BIGSERIAL PRIMARY KEY,
  journey_name  TEXT      NOT NULL
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
  note_type   note_type NOT NULL
);

CREATE TABLE stage (
  id                        BIGSERIAL       PRIMARY KEY,
  journey_id                BIGINT          NOT NULL,
  control_entry_id          BIGINT,
  title                     TEXT,
  explanatory_notes         TEXT,
  question_type             question_type   NOT NULL,
  answer_type               answer_type     NOT NULL,
  next_stage_id             BIGINT,
  go_to_stage_outcome_type  stage_outcome_type
);

CREATE TABLE stage_answer (
  id                                BIGSERIAL   PRIMARY KEY,
  parent_stage_id                   BIGINT      NOT NULL,
  go_to_stage_id                    BIGINT,
  go_to_stage_answer_outcome_type   stage_answer_outcome_type,
  control_entry_id                  BIGINT,
  answer_text                       TEXT,
  display_order                     INT         NOT NULL,
  answer_precedence                 INT,
  divider_above                     BOOLEAN     NOT NULL,
  nested_content                    TEXT,
  more_info_content                 TEXT
);

-- Constraints
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