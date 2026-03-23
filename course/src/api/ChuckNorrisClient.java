package api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.ApiResponse;
import util.HttpClientUtil;
import util.JsonUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ChuckNorrisClient extends AbstractApiClient
{
  private static final String BASE_URL = "https://api.chucknorris.io/jokes/random";

  public ChuckNorrisClient(HttpClientUtil httpClient, JsonUtil jsonUtil)
  {
    super(httpClient, jsonUtil);
  }

  @Override
  public String getSourceName()
  {
    return "chucknorris";
  }

  @Override
  protected String buildUrl(Map< String, String > queryParams)
  {
    if (queryParams == null || queryParams.isEmpty())
    {
      return BASE_URL;
    }
    return BASE_URL + "?" + queryParams.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
  }

  @Override
  public Map< String, Object > flattenResponse(ApiResponse response)
  {
    Map< String, Object > flat = new HashMap<>();
    flat.put("id", response.getData().path("id").asText());
    flat.put("value", response.getData().path("value").asText());
    flat.put("created_at", response.getData().path("created_at").asText());
    flat.put("categories", response.getData().path("categories").toString());
    return flat;
  }

  @Override
  public JsonNode unflatten(Map< String, Object > flat)
  {
    ObjectNode node = jsonUtil.getMapper().createObjectNode();
    node.put("id", (String) flat.get("id"));
    node.put("value", (String) flat.get("value"));
    node.put("created_at", (String) flat.get("created_at"));
    node.put("categories", (String) flat.get("categories"));
    return node;
  }
}
