package config;

import api.ApiClient;
import api.ChuckNorrisClient;
import api.ZipCodeClient;
import api.RandomUserClient;
import util.HttpClientUtil;
import util.JsonUtil;

import java.util.ArrayList;
import java.util.List;

public class AppConfig
{
  private final List< ApiClient > apiClients;

  public AppConfig()
  {
    HttpClientUtil httpClient = new HttpClientUtil();
    JsonUtil jsonUtil = new JsonUtil();

    apiClients = new ArrayList<>();
    apiClients.add(new ChuckNorrisClient(httpClient, jsonUtil));
    apiClients.add(new ZipCodeClient(httpClient, jsonUtil));
    apiClients.add(new RandomUserClient(httpClient, jsonUtil));
  }

  public List< ApiClient > getApiClients()
  {
    return apiClients;
  }
}
