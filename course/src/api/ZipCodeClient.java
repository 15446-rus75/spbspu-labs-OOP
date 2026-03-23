package api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.ApiResponse;
import util.HttpClientUtil;
import util.JsonUtil;
import java.util.HashMap;
import java.util.Map;

public class ZipCodeClient extends AbstractApiClient
{
  private static final String BASE_URL = "http://api.zippopotam.us/us/";

  public ZipCodeClient(HttpClientUtil httpClient, JsonUtil jsonUtil)
  {
    super(httpClient, jsonUtil);
  }

  @Override
  public String getSourceName()
  {
    return "zippopotam";
  }

  @Override
  protected String buildUrl(Map< String, String > queryParams)
  {
    String zip = (queryParams != null && queryParams.containsKey("zip")) ? queryParams.get("zip") : "90210";
    return BASE_URL + zip;
  }

  @Override
  public Map< String, Object > flattenResponse(ApiResponse response)
  {
    Map< String, Object > flat = new HashMap<>();
    JsonNode data = response.getData();
    flat.put("post_code", data.path("post code").asText());
    flat.put("country", data.path("country").asText());
    flat.put("country_abbreviation", data.path("country abbreviation").asText());
    JsonNode places = data.path("places");
    if (places.isArray() && places.size() > 0)
    {
      JsonNode firstPlace = places.get(0);
      flat.put("place_name", firstPlace.path("place name").asText());
      flat.put("state", firstPlace.path("state").asText());
      flat.put("state_abbreviation", firstPlace.path("state abbreviation").asText());
      flat.put("latitude", firstPlace.path("latitude").asText());
      flat.put("longitude", firstPlace.path("longitude").asText());
    }
    return flat;
  }

  @Override
  public JsonNode unflatten(Map< String, Object > flat)
  {
    ObjectNode node = jsonUtil.getMapper().createObjectNode();
    node.put("post code", (String) flat.get("post_code"));
    node.put("country", (String) flat.get("country"));
    node.put("country abbreviation", (String) flat.get("country_abbreviation"));
    ArrayNode places = jsonUtil.getMapper().createArrayNode();
    ObjectNode place = jsonUtil.getMapper().createObjectNode();
    place.put("place name", (String) flat.get("place_name"));
    place.put("state", (String) flat.get("state"));
    place.put("state abbreviation", (String) flat.get("state_abbreviation"));
    place.put("latitude", (String) flat.get("latitude"));
    place.put("longitude", (String) flat.get("longitude"));
    places.add(place);
    node.set("places", places);
    return node;
  }
}
