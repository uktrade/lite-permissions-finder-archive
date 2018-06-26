package controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import components.persistence.LicenceFinderDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.mvc.Http;

@RunWith(MockitoJUnitRunner.class)
public class GuardActionTest {

  @Mock
  private LicenceFinderDao licenceFinderDao;

  @Mock
  private views.html.licencefinder.errorPage errorPage;

  @Mock
  private LicenceFinderAwaitGuardAction guardAction;


  @Before
  public void setUp() {
    errorPage = mock(views.html.licencefinder.errorPage.class);
    guardAction = new LicenceFinderAwaitGuardAction(licenceFinderDao, errorPage);
  }

  @Test
  public void testWithSessionId() {
    Http.Context context = getMockContext(true);
    boolean hasSessionId = guardAction.hasSessionId(context);
    assertThat(hasSessionId).isTrue();
  }

  @Test
  public void testNoSessionId() {
    Http.Context context = getMockContext(false);
    boolean hasSessionId = guardAction.hasSessionId(context);
    assertThat(hasSessionId).isFalse();
  }

  private Http.Context getMockContext(boolean withSessionId) {
    Http.Request mockRequest = mock(Http.Request.class);
    if(withSessionId) {
      when(mockRequest.getQueryString("sessionId")).thenReturn("sessionId");
    }
    Http.Context mockContext = mock(Http.Context.class);
    when(mockContext.request()).thenReturn(mockRequest);
    return mockContext;
  }
}