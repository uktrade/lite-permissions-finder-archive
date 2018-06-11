ALTER TABLE session DROP COLUMN outcome_type;
ALTER TABLE session DROP COLUMN outcome_html;

CREATE TABLE session_outcome (
  id           BIGSERIAL PRIMARY KEY,
  timestamp    TIMESTAMP NOT NULL DEFAULT current_timestamp,
  session_id   TEXT      NOT NULL UNIQUE,
  user_id      TEXT      NOT NULL,
  customer_id  TEXT      NOT NULL,
  site_id      TEXT      NOT NULL,
  outcome_type TEXT      NOT NULL,
  outcome_html TEXT      NOT NULL
);
