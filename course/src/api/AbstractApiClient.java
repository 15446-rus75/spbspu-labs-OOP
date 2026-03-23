package api;

import exception.ApiException;
import model.ApiResponse;
import util.HttpClientUtil;
import util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;

public abstract class AbstractApiClient implements ApiClient
{
  protected final HttpClientUtil httpClient;
  protected final JsonUtil jsonUtil;

  public AbstractApiClient(HttpClientUtil httpClient, JsonUtil jsonUtil)
  {
    this.httpClient = httpClient;
    this.jsonUtil = jsonUtil;
  }

  protected abstract String buildUrl(Map< String, String > queryParams);

  protected JsonNode extractData(JsonNode root)
  {
    return root;
  }

  @Override
  public ApiResponse fetchData(Map< String, String > queryParams) throws ApiException
  {
    try
    {
      String url = buildUrl(queryParams);
      String jsonResponse = httpClient.get(url);
      JsonNode root = jsonUtil.parse(jsonResponse);
      JsonNode data = extractData(root);
      return new ApiResponse(getSourceName(), Instant.now(), data);
    }
    catch (IOException e)
    {
      throw new ApiException("Ошибка при запросе к " + getSourceName() + ": " + e.getMessage(), e);
    }
  }
}
