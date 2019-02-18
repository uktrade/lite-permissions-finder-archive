package components.services;

import com.google.inject.Inject;
import components.cms.dao.ControlEntryDao;
import lombok.AllArgsConstructor;
import models.callback.ControlEntryResponse;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor(onConstructor = @__({@Inject}))
public class ControlEntryServiceImpl implements ControlEntryService {

  private final ControlEntryDao controlEntryDao;

  @Override
  public List<ControlEntryResponse> findControlEntriesByControlCode(String value) {
    if (StringUtils.isBlank(value)) {
      return new ArrayList<>();
    }

    return controlEntryDao.findControlEntriesByControlCode(value)
      .stream()
      .map(ControlEntryResponse::new)
      .collect(Collectors.toList());
  }
}
