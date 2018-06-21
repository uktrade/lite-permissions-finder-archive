ALTER TABLE stage
  DROP CONSTRAINT question_type_value,
  ADD CONSTRAINT question_type_value CHECK (question_type IN ('STANDARD', 'DECONTROL', 'ITEM'));