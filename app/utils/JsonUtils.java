package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.ListUtils;

import java.io.IOException;
import java.util.List;

public class JsonUtils {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final TypeReference<List<String>> STRING_LIST_TYPE_REFERENCE = new TypeReference<List<String>>() {};

  public static List<String> convertJsonToList(String json) {
    try {
      return OBJECT_MAPPER.readValue(json, STRING_LIST_TYPE_REFERENCE);
    } catch (IOException ioe) {
      throw new RuntimeException("Failed to convert json to list", ioe);
    }
  }

  public static <E> String convertListToJson(List<? extends E> list) {
    return toJson(ListUtils.emptyIfNull(list));
  }

  private static String toJson(Object object) {
    try {
      return OBJECT_MAPPER.writeValueAsString(object);
    } catch (JsonProcessingException jpe) {
      throw new RuntimeException("Failed to convert object to json", jpe);
    }
  }

}
