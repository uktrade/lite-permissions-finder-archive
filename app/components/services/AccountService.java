package components.services;

import models.AccountData;

import java.util.Optional;

public interface AccountService {
  Optional<AccountData> getAccountData(String userId);
}
