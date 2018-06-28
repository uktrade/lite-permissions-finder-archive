package components.services;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import components.common.client.userservice.UserServiceClientBasicAuth;
import components.common.client.userservice.UserServiceClientJwt;
import models.enums.SessionOutcomeType;
import org.junit.Test;
import triage.session.SessionOutcome;
import uk.gov.bis.lite.user.api.view.CustomerView;
import uk.gov.bis.lite.user.api.view.SiteView;
import uk.gov.bis.lite.user.api.view.UserAccountTypeView;
import uk.gov.bis.lite.user.api.view.UserPrivilegesView;
import uk.gov.bis.lite.user.api.view.enums.AccountType;
import uk.gov.bis.lite.user.api.view.enums.Role;

import java.util.Arrays;
import java.util.Collections;

public class UserPrivilegeServiceImplTest {

  private static final String USER_ID = "1";
  private static final String CUSTOMER_ID = "2";
  private static final String OTHER_CUSTOMER_ID = "3";
  private static final String SITE_ID = "4";
  private static final String OTHER_SITE_ID = "5";

  private final UserServiceClientJwt userServiceClient = mock(UserServiceClientJwt.class);
  private final UserServiceClientBasicAuth userServiceClientBasicAuth = mock(UserServiceClientBasicAuth.class);
  private final UserPrivilegeServiceImpl userPrivilegeServiceImpl = new UserPrivilegeServiceImpl(userServiceClient, userServiceClientBasicAuth);
  private final SessionOutcome sessionOutcome = new SessionOutcome("id", "sessionId", USER_ID, CUSTOMER_ID, SITE_ID, SessionOutcomeType.NLR_DECONTROL, "");

  @Test
  public void canViewOutcomeIfRegulator() {
    when(userServiceClient.getUserPrivilegeView(USER_ID)).thenReturn(completedFuture(new UserPrivilegesView()));
    UserAccountTypeView userAccountTypeView = new UserAccountTypeView();
    userAccountTypeView.setAccountType(AccountType.REGULATOR);
    when(userServiceClientBasicAuth.getUserAccountTypeView(eq(USER_ID))).thenReturn(completedFuture(userAccountTypeView));

    boolean canViewOutcome = userPrivilegeServiceImpl.canViewOutcome(USER_ID, sessionOutcome);
    assertThat(canViewOutcome).isTrue();
  }

  @Test
  public void cannotViewOutcomeIfExporterWithoutPrivileges() {
    when(userServiceClient.getUserPrivilegeView(USER_ID)).thenReturn(completedFuture(new UserPrivilegesView()));
    UserAccountTypeView userAccountTypeView = new UserAccountTypeView();
    userAccountTypeView.setAccountType(AccountType.EXPORTER);
    when(userServiceClientBasicAuth.getUserAccountTypeView(eq(USER_ID))).thenReturn(completedFuture(userAccountTypeView));

    boolean canViewOutcome = userPrivilegeServiceImpl.canViewOutcome(USER_ID, sessionOutcome);
    assertThat(canViewOutcome).isFalse();
  }

  @Test
  public void cannotViewOutcomeIfUnknownAccountTypeWithoutPrivileges() {
    when(userServiceClient.getUserPrivilegeView(USER_ID)).thenReturn(completedFuture(new UserPrivilegesView()));
    UserAccountTypeView userAccountTypeView = new UserAccountTypeView();
    userAccountTypeView.setAccountType(AccountType.UNKNOWN);
    when(userServiceClientBasicAuth.getUserAccountTypeView(eq(USER_ID))).thenReturn(completedFuture(userAccountTypeView));

    boolean canViewOutcome = userPrivilegeServiceImpl.canViewOutcome(USER_ID, sessionOutcome);
    assertThat(canViewOutcome).isFalse();
  }

  @Test
  public void canViewOutcomeIfHasCustomerRole() {
    for (Role role : Arrays.asList(Role.ADMIN, Role.SUBMITTER, Role.PREPARER)) {
      CustomerView customerView = new CustomerView();
      customerView.setRole(role);
      customerView.setCustomerId(CUSTOMER_ID);
      UserPrivilegesView userPrivilegesView = new UserPrivilegesView();
      userPrivilegesView.setCustomers(Collections.singletonList(customerView));
      when(userServiceClient.getUserPrivilegeView(USER_ID)).thenReturn(completedFuture(userPrivilegesView));
      when(userServiceClientBasicAuth.getUserAccountTypeView(eq(USER_ID))).thenReturn(completedFuture(new UserAccountTypeView()));

      boolean canViewOutcome = userPrivilegeServiceImpl.canViewOutcome(USER_ID, sessionOutcome);
      assertThat(canViewOutcome).isTrue();
    }
  }

  @Test
  public void cannotViewOutcomeIfNonMatchingCustomerId() {
    CustomerView customerView = new CustomerView();
    customerView.setRole(Role.ADMIN);
    customerView.setCustomerId(OTHER_CUSTOMER_ID);
    UserPrivilegesView userPrivilegesView = new UserPrivilegesView();
    userPrivilegesView.setCustomers(Collections.singletonList(customerView));
    when(userServiceClient.getUserPrivilegeView(USER_ID)).thenReturn(completedFuture(userPrivilegesView));
    when(userServiceClientBasicAuth.getUserAccountTypeView(eq(USER_ID))).thenReturn(completedFuture(new UserAccountTypeView()));

    boolean canViewOutcome = userPrivilegeServiceImpl.canViewOutcome(USER_ID, sessionOutcome);
    assertThat(canViewOutcome).isFalse();
  }

  @Test
  public void canViewOutcomeIfHasSiteRole() {
    for (Role role : Arrays.asList(Role.ADMIN, Role.SUBMITTER, Role.PREPARER)) {
      SiteView siteView = new SiteView();
      siteView.setRole(role);
      siteView.setSiteId(SITE_ID);
      UserPrivilegesView userPrivilegesView = new UserPrivilegesView();
      userPrivilegesView.setSites(Collections.singletonList(siteView));
      when(userServiceClient.getUserPrivilegeView(USER_ID)).thenReturn(completedFuture(userPrivilegesView));
      when(userServiceClientBasicAuth.getUserAccountTypeView(eq(USER_ID))).thenReturn(completedFuture(new UserAccountTypeView()));

      boolean canViewOutcome = userPrivilegeServiceImpl.canViewOutcome(USER_ID, sessionOutcome);
      assertThat(canViewOutcome).isTrue();
    }
  }

  @Test
  public void cannotViewOutcomeIfNonMatchingSiteId() {
    SiteView siteView = new SiteView();
    siteView.setRole(Role.ADMIN);
    siteView.setSiteId(OTHER_SITE_ID);
    UserPrivilegesView userPrivilegesView = new UserPrivilegesView();
    userPrivilegesView.setSites(Collections.singletonList(siteView));
    when(userServiceClient.getUserPrivilegeView(USER_ID)).thenReturn(completedFuture(userPrivilegesView));
    when(userServiceClientBasicAuth.getUserAccountTypeView(eq(USER_ID))).thenReturn(completedFuture(new UserAccountTypeView()));

    boolean canViewOutcome = userPrivilegeServiceImpl.canViewOutcome(USER_ID, sessionOutcome);
    assertThat(canViewOutcome).isFalse();
  }

}
