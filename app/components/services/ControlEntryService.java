package components.services;

import java.util.List;

import models.callback.ControlEntryResponse;

public interface ControlEntryService {
  List<ControlEntryResponse> findControlEntriesByControlCode(String value);
}
