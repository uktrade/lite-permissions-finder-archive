package components.services;

import models.admin.AdminCheckResult;

public interface PingService {

  AdminCheckResult adminCheck(String adminCheckPath);
}
