package api;

import com.fasterxml.jackson.databind.JsonNode;
import exception.ApiException;
import model.ApiResponse;
import java.util.Map;

public interface ApiClient
{
  String getSourceName();
  ApiResponse fetchData(Map< String, String > queryParams) throws ApiException;
  Map< String, Object > flattenResponse(ApiResponse response);
  JsonNode unflatten(Map< String, Object > flat);
}
