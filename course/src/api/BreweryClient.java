package api;

import exception.ApiException;
import model.ApiResponse;
import util.HttpClientUtil;
import util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class BreweryClient extends AbstractApiClient
{
  private static final String BASE_URL = "https://api.openbrewerydb.org/breweries";

  public BreweryClient(HttpClientUtil httpClient, JsonUtil jsonUtil)
  {
    super(httpClient, jsonUtil);
  }

  @Override
  public String getSourceName()
  {
    return "openbrewerydb";
  }

  @Override
  protected String buildUrl(Map<String, String> queryParams)
  {
    if (queryParams == null || queryParams.isEmpty())
    {
      return BASE_URL + "?per_page=1";
    }
    StringBuilder url = new StringBuilder(BASE_URL);
    String sep = "?";
    for (Map.Entry<String, String> entry : queryParams.entrySet())
    {
      url.append(sep).append(entry.getKey()).append("=").append(entry.getValue());
      sep = "&";
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
      if (root.isArray() && root.size() > 0)
      {
        data = root.get(0);
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
    flat.put("id", data.path("id").asText());
    flat.put("name", data.path("name").asText());
    flat.put("brewery_type", data.path("brewery_type").asText());
    flat.put("city", data.path("city").asText());
    flat.put("state", data.path("state").asText());
    flat.put("phone", data.path("phone").asText());
    return flat;
  }
}
