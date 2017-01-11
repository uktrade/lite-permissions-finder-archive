package components.services.controlcode;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FrontendControlCode {

  public final ControlCodeData controlCodeData;

  public final Optional<Ancestor> greatestAncestor;

  public final List<Ancestor> otherAncestors;

  public FrontendControlCode(@JsonProperty("controlCodeData") ControlCodeData controlCodeData,
                             @JsonProperty("lineage")List<Ancestor> ancestors) {
    this.controlCodeData = controlCodeData;
    List<Ancestor> reversedAncestors = Lists.reverse(ancestors);
    this.greatestAncestor = !reversedAncestors.isEmpty() ? Optional.of(reversedAncestors.get(0)) : Optional.empty();
    this.otherAncestors = reversedAncestors.size() > 1
        ? reversedAncestors.subList(1, reversedAncestors.size())
        : Collections.emptyList();
  }

}