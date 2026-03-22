package config;

import api.ApiClient;
import api.BreweryClient;
import api.ChuckNorrisClient;
import api.ZipCodeClient;
import util.HttpClientUtil;
import util.JsonUtil;

import java.util.ArrayList;
import java.util.List;

public class AppConfig
{
  private final List<ApiClient> apiClients;

  public AppConfig()
  {
    HttpClientUtil httpClient = new HttpClientUtil();
    JsonUtil jsonUtil = new JsonUtil();

    apiClients = new ArrayList<>();
    apiClients.add(new ChuckNorrisClient(httpClient, jsonUtil));
    apiClients.add(new ZipCodeClient(httpClient, jsonUtil));
    apiClients.add(new BreweryClient(httpClient, jsonUtil));
  }

  public List<ApiClient> getApiClients()
  {
    return apiClients;
  }
}
