package components.services;

import components.cms.dao.ControlEntryDao;
import models.callback.ControlEntryResponse;
import models.cms.ControlEntry;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ControlEntryServiceTest {

  private static final String CONTROL_CODE_A = "ML1a";
  private static final String CONTROL_CODE_B = "ML1b";
  private final ControlEntryDao controlEntryDaoMock = mock(ControlEntryDao.class);
  private final ControlEntryService controlEntryService = new ControlEntryServiceImpl(controlEntryDaoMock);

  @Test
  public void whenNothingInTheDB_searchForAnyControlCodeReturnsNoResults() {
    when(controlEntryDaoMock.findControlEntriesByControlCode(CONTROL_CODE_A))
      .thenReturn(Collections.EMPTY_LIST);
    List<ControlEntryResponse> result = controlEntryService.findControlEntriesByControlCode(CONTROL_CODE_A);
    assertThat(result).isEmpty();
  }

  @Test
  public void whenCodeAInTheDB_searchForCodeBReturnsNoResults() {
    when(controlEntryDaoMock.findControlEntriesByControlCode(CONTROL_CODE_A))
      .thenReturn(Collections.singletonList(new ControlEntry()));
    List<ControlEntryResponse> result = controlEntryService.findControlEntriesByControlCode(CONTROL_CODE_B);
    assertThat(result).isEmpty();
  }

  @Test
  public void whenCodeAandBInTheDB_searchForCodeBReturnsCodeBResult() {
    List<ControlEntry> response = new ArrayList<>();
    ControlEntry controlEntry = new ControlEntry();
    controlEntry.setControlCode(CONTROL_CODE_B);
    response.add(controlEntry);
    when(controlEntryDaoMock.findControlEntriesByControlCode(CONTROL_CODE_B))
      .thenReturn(response);
    List<ControlEntryResponse> result = controlEntryService.findControlEntriesByControlCode(CONTROL_CODE_B);
    assertThat(result.size() == 1).isTrue();
    assertThat(result.get(0).getControlCode()).isEqualTo(CONTROL_CODE_B);
  }

  @Test
  public void whenTheQueryIsBlank_searchReturnsNoResults() {
    when(controlEntryDaoMock.findControlEntriesByControlCode(CONTROL_CODE_A)).thenReturn(Collections.EMPTY_LIST);
    List<ControlEntryResponse> result = controlEntryService.findControlEntriesByControlCode(CONTROL_CODE_A);
    assertThat(result).isEmpty();
  }
}
