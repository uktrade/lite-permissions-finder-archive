ALTER TYPE outcome_type RENAME TO stage_answer_outcome_type;

ALTER TABLE stage_answer RENAME COLUMN go_to_outcome_type TO go_to_stage_answer_outcome_type;

CREATE TYPE stage_outcome_type AS ENUM ('DECONTROL');

ALTER TABLE stage
  ADD COLUMN go_to_stage_outcome_type stage_outcome_type
, DROP CONSTRAINT stage_ck_1
, ADD CONSTRAINT stage_ck_1 CHECK (
  (question_type = 'DECONTROL' AND (next_stage_id IS NOT NULL OR go_to_stage_outcome_type = 'DECONTROL'))
  OR (question_type != 'DECONTROL' and go_to_stage_outcome_type IS NULL)
);
