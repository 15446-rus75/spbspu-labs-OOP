package api;

import exception.ApiException;
import model.ApiResponse;
import java.util.Map;

public interface ApiClient
{
  String getSourceName();
  ApiResponse fetchData(Map<String, String> queryParams) throws ApiException;
  Map<String, Object> flattenResponse(ApiResponse response);
}
