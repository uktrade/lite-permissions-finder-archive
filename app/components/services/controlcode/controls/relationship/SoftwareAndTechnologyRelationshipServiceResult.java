package components.services.controlcode.controls.relationship;

import com.fasterxml.jackson.databind.JsonNode;

public class SoftwareAndTechnologyRelationshipServiceResult {
  public final boolean relationshipExists;
  public final String relationshipDetails;

  public SoftwareAndTechnologyRelationshipServiceResult(JsonNode responseJson) {
    relationshipExists = false;
    relationshipDetails = null;
  }

  public SoftwareAndTechnologyRelationshipServiceResult(boolean relationshipExists) {
    this.relationshipExists = relationshipExists;
    if (relationshipExists) {
      relationshipDetails = "DETAILS OF A RELATIONSHIP BETWEEN SOFTWARE AND TECHNOLOGY CATEGORY";
    }
    else {
      relationshipDetails = null;
    }
  }
}
