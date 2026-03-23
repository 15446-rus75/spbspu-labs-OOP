package api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.ApiResponse;
import util.HttpClientUtil;
import util.JsonUtil;
import java.util.HashMap;
import java.util.Map;

public class RandomUserClient extends AbstractApiClient
{
  private static final String BASE_URL = "https://randomuser.me/api/";

  public RandomUserClient(HttpClientUtil httpClient, JsonUtil jsonUtil)
  {
    super(httpClient, jsonUtil);
  }

  @Override
  public String getSourceName()
  {
    return "randomuser";
  }

  @Override
  protected String buildUrl(Map< String, String > queryParams)
  {
    return buildUrlWithQueryParams(BASE_URL, queryParams);
  }

  @Override
  protected JsonNode extractData(JsonNode root)
  {
    if (root.has("results") && root.get("results").isArray() && root.get("results").size() > 0)
    {
      return root.get("results").get(0);
    }
    return root;
  }

  @Override
  public Map< String, Object > flattenResponse(ApiResponse response)
  {
    Map< String, Object > flat = new HashMap<>();
    JsonNode data = response.getData();
    flat.put("gender", data.path("gender").asText());
    flat.put("title", data.path("name").path("title").asText());
    flat.put("first_name", data.path("name").path("first").asText());
    flat.put("last_name", data.path("name").path("last").asText());
    flat.put("email", data.path("email").asText());
    flat.put("country", data.path("location").path("country").asText());
    flat.put("phone", data.path("phone").asText());
    flat.put("city", data.path("location").path("city").asText());
    return flat;
  }

  @Override
  public JsonNode unflatten(Map< String, Object > flat)
  {
    ObjectNode node = jsonUtil.getMapper().createObjectNode();
    node.put("gender", (String) flat.get("gender"));
    ObjectNode nameNode = jsonUtil.getMapper().createObjectNode();
    nameNode.put("title", (String) flat.get("title"));
    nameNode.put("first", (String) flat.get("first_name"));
    nameNode.put("last", (String) flat.get("last_name"));
    node.set("name", nameNode);
    node.put("email", (String) flat.get("email"));
    ObjectNode locationNode = jsonUtil.getMapper().createObjectNode();
    locationNode.put("country", (String) flat.get("country"));
    locationNode.put("city", (String) flat.get("city"));
    node.set("location", locationNode);
    node.put("phone", (String) flat.get("phone"));
    return node;
  }
}
