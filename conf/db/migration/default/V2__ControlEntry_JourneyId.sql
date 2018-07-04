ALTER TABLE control_entry
  ADD COLUMN journey_id BIGINT NOT NULL,
  ADD CONSTRAINT journeyfk FOREIGN KEY (journey_id) REFERENCES journey(id);
