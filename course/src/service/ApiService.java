package service;

import api.ApiClient;
import exception.ApiException;
import model.ApiResponse;
import model.AggregatedRecord;
import util.HttpClientUtil;
import util.JsonUtil;

import java.util.*;

public class ApiService
{
  private final Map< String, ApiClient > clients = new HashMap<>();
  private final HttpClientUtil httpClient;
  private final JsonUtil jsonUtil;

  public ApiService(List< ApiClient > clientList, HttpClientUtil httpClient, JsonUtil jsonUtil)
  {
    for (ApiClient client : clientList)
    {
      clients.put(client.getSourceName(), client);
    }
    this.httpClient = httpClient;
    this.jsonUtil = jsonUtil;
  }

  public List< String > getAvailableApiNames()
  {
    return new ArrayList<>(clients.keySet());
  }

  public ApiClient getClient(String name)
  {
    return clients.get(name);
  }

  public List< AggregatedRecord > fetchDataFromApis(List< String > apiNames, Map< String, Map< String, String > > params)
  {
    List< AggregatedRecord > records = new ArrayList<>();
    for (String name : apiNames)
    {
      ApiClient client = clients.get(name);
      if (client == null)
      {
        System.err.println("Предупреждение: API '" + name + "' не найден");
        continue;
      }
      try
      {
        Map< String, String > queryParams = params.getOrDefault(name, new HashMap<>());
        ApiResponse response = client.fetchData(queryParams);
        records.add(new AggregatedRecord(name, response.getTimestamp(), response.getData()));
      }
      catch (ApiException e)
      {
        System.err.println("Ошибка при получении данных от " + name + ": " + e.getMessage());
      }
    }
    return records;
  }
}
