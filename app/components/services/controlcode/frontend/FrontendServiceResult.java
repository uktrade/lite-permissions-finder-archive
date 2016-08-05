package components.services.controlcode.frontend;

import com.fasterxml.jackson.annotation.JsonProperty;
import scala.Option;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FrontendServiceResult {

  @JsonProperty("controlCodeData")
  public ControlCodeData controlCodeData;

  @JsonProperty("lineage")
  public List<Ancestor> ancestors;

  public FrontendServiceResult(){}

  public Option<Ancestor> getGreatestAncestor() {
    return !ancestors.isEmpty() ? Option.apply(ancestors.get(ancestors.size() -1)) : Option.empty();
  }

  public List<Ancestor> getOtherAncestors() {
    return IntStream.range(0, ancestors.size() - 1)
        .mapToObj(i -> ancestors.get(i))
        .collect(Collectors.toList());
  }

}