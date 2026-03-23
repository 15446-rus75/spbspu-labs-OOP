package api;

import exception.ApiException;
import model.ApiResponse;
import util.HttpClientUtil;
import util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
  protected String buildUrl(Map<String, String> queryParams)
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
  protected JsonNode extractData(JsonNode root)
  {
    if (root.has("results") && root.get("results").isArray() && root.get("results").size() > 0)
    {
      return root.get("results").get(0);
    }
    return root;
  }

  @Override
  public Map<String, Object> flattenResponse(ApiResponse response)
  {
    Map<String, Object> flat = new HashMap<>();
    JsonNode data = response.getData();
    flat.put("gender", data.path("gender").asText());
    flat.put("title", data.path("name").path("title").asText());
    flat.put("first_name", data.path("name").path("first").asText());
    flat.put("last_name", data.path("name").path("last").asText());
    flat.put("email", data.path("email").asText());
    flat.put("country", data.path("location").path("country").asText());
    flat.put("phone", data.path("phone").asText());
    return flat;
  }
}
