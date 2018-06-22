CREATE TABLE related_control_entry (
  control_entry_id         BIGINT NOT NULL REFERENCES control_entry(id),
  related_control_entry_id BIGINT NOT NULL REFERENCES control_entry(id),
  PRIMARY KEY(control_entry_id, related_control_entry_id)
);
