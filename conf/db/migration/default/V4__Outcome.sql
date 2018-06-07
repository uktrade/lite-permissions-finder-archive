ALTER TABLE session DROP COLUMN outcome_type;
ALTER TABLE session DROP COLUMN outcome_html;

CREATE TABLE session_outcome (
  id           TEXT      NOT NULL UNIQUE,
  timestamp    TIMESTAMP NOT NULL DEFAULT current_timestamp,
  session_id   TEXT      NOT NULL,
  user_id      TEXT      NOT NULL,
  customer_id  TEXT      NOT NULL,
  site_id      TEXT      NOT NULL,
  outcome_type TEXT      NOT NULL,
  outcome_html TEXT      NOT NULL
);
