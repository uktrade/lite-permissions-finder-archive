CREATE TABLE session (
  id           TEXT      NOT NULL UNIQUE,
  timestamp    TIMESTAMP NOT NULL DEFAULT current_timestamp,
  journey_id   BIGINT    NOT NULL,
  resume_code  TEXT      NOT NULL UNIQUE,
  outcome_type TEXT,
  outcome_html TEXT
);

CREATE TABLE session_stage (
  session_id  TEXT   NOT NULL,
  stage_id    BIGINT NOT NULL,
  answer_json TEXT   NOT NULL,
  PRIMARY KEY (session_id, stage_id),
  FOREIGN KEY (session_id) REFERENCES session(id),
  FOREIGN KEY (stage_id)   REFERENCES stage(id)
);
