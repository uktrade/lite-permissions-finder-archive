package components.services.controlcode;

import uk.gov.bis.lite.controlcode.api.view.ControlCodeFullView;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ControlCodeFullViewResult {
  private final List<ControlCodeFullView> controlCodes;

  public ControlCodeFullViewResult(List<ControlCodeFullView> controlCodes) {
    this.controlCodes = controlCodes;
  }

  public boolean hasRelatedCodes(String controlCode) {
    String title = lookupTitle(controlCode);
    return this.controlCodes.stream().filter(cc -> title.equals(cc.getTitle())).count() > 1;
  }

  public List<ControlCodeFullView> getControlCodes() {
    return controlCodes;
  }

  public List<ControlCodeFullView> getControlCodesGroupedByTitle() {
    Set<String> seenTitles = new HashSet<>();
    return this.controlCodes.stream()
        .filter(cc -> seenTitles.add(cc.getTitle()))
        .collect(Collectors.toList());
  }

  public List<ControlCodeFullView> getRelatedControlCodes(String controlCode) {
    String title = lookupTitle(controlCode);
    return controlCodes.stream()
        .filter(cc -> title.equals(cc.getTitle()))
        .sorted(Comparator.comparing(ControlCodeFullView::getControlCode))
        .collect(Collectors.toList());
  }

  private String lookupTitle(String controlCode) {
    Optional<ControlCodeFullView> controlCodeFullViewOptional = this.controlCodes.stream()
        .filter(cc -> cc.getControlCode().equals(controlCode))
        .findAny();
    if (controlCodeFullViewOptional.isPresent()) {
      return controlCodeFullViewOptional.get().getTitle();
    }
    else {
      throw new RuntimeException(String.format("Control code %s not found", controlCode));
    }
  }

}
