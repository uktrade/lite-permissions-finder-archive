ALTER TABLE stage
  DROP CONSTRAINT go_to_stage_outcome_type_value,
  ADD CONSTRAINT go_to_stage_outcome_type_value CHECK (go_to_stage_outcome_type IN ('CONTROL_ENTRY_FOUND', 'TOO_COMPLEX')),
  DROP CONSTRAINT stage_ck_1,
  ADD CONSTRAINT stage_ck_1 CHECK (
    (
      question_type = 'DECONTROL'
      AND (
        next_stage_id IS NOT NULL
        OR (
          go_to_stage_outcome_type = 'CONTROL_ENTRY_FOUND' OR go_to_stage_outcome_type = 'TOO_COMPLEX'
        )
      )
    )
    OR (question_type != 'DECONTROL' AND go_to_stage_outcome_type IS NULL)
);