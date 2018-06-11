package components.services;

import triage.session.SessionOutcome;

public interface UserPrivilegeService {

  boolean canViewOutcome(SessionOutcome sessionOutcome);

}
