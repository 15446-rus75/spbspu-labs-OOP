package api;

import exception.ApiException;
import model.ApiResponse;
import util.HttpClientUtil;
import util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
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
  protected String buildUrl(Map<String, String> queryParams)
  {
    StringBuilder url = new StringBuilder(BASE_URL);
    if (queryParams != null && !queryParams.isEmpty())
    {
      url.append("?");
      queryParams.entrySet().forEach(entry ->
                url.append(entry.getKey()).append("=").append(entry.getValue()).append("&"));
            url.deleteCharAt(url.length() - 1);
    }
    return url.toString();
  }

  @Override
  public ApiResponse fetchData(Map<String, String> queryParams) throws ApiException
  {
    String url = buildUrl(queryParams);
    try
    {
      String jsonResponse = httpClient.get(url);
      JsonNode root = jsonUtil.parse(jsonResponse);
      JsonNode data;
      if (root.has("results") && root.get("results").isArray() && root.get("results").size() > 0)
      {
        data = root.get("results").get(0);
      }
      else
      {
        data = root;
      }
      return new ApiResponse(getSourceName(), Instant.now(), data);
    }
    catch (Exception e)
    {
      throw new ApiException("Ошибка при запросе к " + getSourceName(), e);
    }
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
