package components.services.controlcode.controls.relationships;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;

import java.util.Arrays;
import java.util.List;

public class GoodsRelationshipsServiceResult {

  public final List<GoodsRelationship> relationships;

  public GoodsRelationshipsServiceResult(JsonNode responseJson) {
    this.relationships = Arrays.asList(Json.fromJson(responseJson, GoodsRelationship[].class));
  }

  public boolean relationshipsExist() {
    return relationships != null &&  !relationships.isEmpty();
  }

  public GoodsRelationship getRelationship(int index) {
    return this.relationships.get(index);
  }

  public boolean hasNextRelationship(int currentIndex) {
    return isValidRelationshipIndex(currentIndex + 1);
  }

  public boolean isValidRelationshipIndex(int currentIndex) {
    return currentIndex >= 0 && currentIndex <= relationships.size() -1;
  }
}
