package controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import components.common.auth.AuthInfo;
import components.common.auth.SpireAuthManager;
import components.persistence.LicenceFinderDao;
import models.persistence.RegisterLicence;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.mvc.Http;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class GuardActionTest {

  @Mock
  private SpireAuthManager authManager;

  @Mock
  private AuthInfo authInfo;

  @Mock
  private LicenceFinderDao licenceFinderDao;

  @Mock
  private views.html.licencefinder.errorPage errorPage;

  @Mock
  private LicenceFinderAwaitGuardAction awaitGuardAction;

  @Mock
  private LicenceFinderUserGuardAction userGuardAction;

  private final String TEST_USER1_ID = "TEST_USER1_ID";
  private final String TEST_USER2_ID = "TEST_USER2_ID";
  private final String SESSION_ID_KEY = "sessionId";
  private final String SESSION_ID = "SESSION_ID";

  @Before
  public void setUp() {
    errorPage = mock(views.html.licencefinder.errorPage.class);
    awaitGuardAction = new LicenceFinderAwaitGuardAction(licenceFinderDao, errorPage);
    userGuardAction = new LicenceFinderUserGuardAction(licenceFinderDao, authManager, errorPage);
  }

  @Test
  public void testAwaitWithSessionId() {
    assertThat(awaitGuardAction.hasSessionId(getMockContextWithSession())).isTrue();
  }

  @Test
  public void testAwaitNoSessionId() {
    assertThat(awaitGuardAction.hasSessionId(getMockContextWithoutSession())).isFalse();
  }

  @Test
  public void testWithRegisterLicence() {
    when(licenceFinderDao.getRegisterLicence(SESSION_ID)).thenReturn(Optional.of(new RegisterLicence()));
    assertThat(awaitGuardAction.hasRegisterLicence(SESSION_ID)).isTrue();
  }

  @Test
  public void testWithoutRegisterLicence() {
    when(licenceFinderDao.getRegisterLicence(SESSION_ID)).thenReturn(Optional.empty());
    assertThat(awaitGuardAction.hasRegisterLicence(SESSION_ID)).isFalse();
  }

  @Test
  public void testUserWithSessionId() {
    assertThat(userGuardAction.hasSessionId(getMockContextWithSession())).isTrue();
  }

  @Test
  public void testUserNoSessionId() {
    assertThat(userGuardAction.hasSessionId(getMockContextWithoutSession())).isFalse();
  }

  @Test
  public void testUserMatchingIds() {
    setUpAuthInfo(TEST_USER1_ID);
    when(licenceFinderDao.getUserId(SESSION_ID)).thenReturn(TEST_USER1_ID);
    assertThat(userGuardAction.hasMatchingUserIds(getMockContextWithSession())).isTrue();
  }

  @Test
  public void testUserNonMatchingIds() {
    setUpAuthInfo(TEST_USER1_ID);
    when(licenceFinderDao.getUserId(SESSION_ID)).thenReturn(TEST_USER2_ID);
    assertThat(userGuardAction.hasMatchingUserIds(getMockContextWithSession())).isFalse();
  }

  /**
   * Private methods
   */
  private void setUpAuthInfo(String userId) {
    when(authInfo.getId()).thenReturn(userId);
    when(authManager.getAuthInfoFromContext()).thenReturn(authInfo);
  }

  private Http.Context getMockContextWithSession() {
    return getMockContext(true);
  }

  private Http.Context getMockContextWithoutSession() {
    return getMockContext(false);
  }

  private Http.Context getMockContext(boolean withSessionId) {
    Http.Request mockRequest = mock(Http.Request.class);
    if (withSessionId) {
      when(mockRequest.getQueryString(SESSION_ID_KEY)).thenReturn(SESSION_ID);
    }
    Http.Context mockContext = mock(Http.Context.class);
    when(mockContext.request()).thenReturn(mockRequest);
    return mockContext;
  }
}