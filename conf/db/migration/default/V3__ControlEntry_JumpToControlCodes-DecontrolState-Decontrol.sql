ALTER TABLE control_entry
  ADD COLUMN jump_to_control_codes TEXT;
  ADD COLUMN decontrol BOOLEAN DEFAULT FALSE;

ALTER TABLE session
  ADD COLUMN decontrol_state TEXT;
