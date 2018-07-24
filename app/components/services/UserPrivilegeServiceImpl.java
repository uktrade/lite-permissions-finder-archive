package components.services;

import com.google.inject.Inject;
import components.common.client.UserServiceClientBasicAuth;
import components.common.client.UserServiceClientJwt;
import triage.session.SessionOutcome;
import uk.gov.bis.lite.user.api.view.UserAccountTypeView;
import uk.gov.bis.lite.user.api.view.UserPrivilegesView;
import uk.gov.bis.lite.user.api.view.enums.AccountType;
import uk.gov.bis.lite.user.api.view.enums.Role;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class UserPrivilegeServiceImpl implements UserPrivilegeService {

  private static final Set<Role> VIEW_OUTCOME_ROLES = EnumSet.of(Role.ADMIN, Role.SUBMITTER, Role.PREPARER);

  private final UserServiceClientJwt userServiceClient;
  private final UserServiceClientBasicAuth userServiceClientBasicAuth;

  @Inject
  public UserPrivilegeServiceImpl(UserServiceClientJwt userServiceClient,
                                  UserServiceClientBasicAuth userServiceClientBasicAuth) {
    this.userServiceClient = userServiceClient;
    this.userServiceClientBasicAuth = userServiceClientBasicAuth;
  }

  private UserPrivilegesView getUserPrivilegesView(String userId) {
    try {
      return userServiceClient.getUserPrivilegeView(userId).toCompletableFuture().get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException("Unable to get userPrivilegesView for userId " + userId, e);
    }
  }

  private UserAccountTypeView getUserAccountTypeView(String userId) {
    try {
      return userServiceClientBasicAuth.getUserAccountTypeView(userId).toCompletableFuture().get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException("Unable to get userAccountTypeView for userId " + userId, e);
    }
  }

  @Override
  public boolean canViewOutcome(String userId, SessionOutcome sessionOutcome) {
    UserAccountTypeView userAccountTypeView = getUserAccountTypeView(userId);
    UserPrivilegesView userPrivilegesView = getUserPrivilegesView(userId);
    return userAccountTypeView.getAccountType() == AccountType.REGULATOR ||
        hasSiteRole(userPrivilegesView, sessionOutcome.getSiteId(), VIEW_OUTCOME_ROLES) ||
        hasCustomerRole(userPrivilegesView, sessionOutcome.getCustomerId(), VIEW_OUTCOME_ROLES);
  }

  private boolean hasSiteRole(UserPrivilegesView userPrivilegesView, String siteId, Set<Role> roles) {
    return userPrivilegesView.getSites().stream().anyMatch(view -> view.getSiteId().equals(siteId) && roles.contains(view.getRole()));
  }

  private boolean hasCustomerRole(UserPrivilegesView userPrivilegesView, String customerId, Set<Role> roles) {
    return userPrivilegesView.getCustomers().stream()
        .anyMatch(view -> view.getCustomerId().equals(customerId) && roles.contains(view.getRole()));
  }

}
