package config;

import api.ApiClient;
import org.junit.jupiter.api.Test;
import util.HttpClientUtil;
import util.JsonUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest
{

  @Test
  void getApiClients_shouldReturnThreeClients()
  {
    HttpClientUtil httpClient = new HttpClientUtil();
    JsonUtil jsonUtil = new JsonUtil();
    AppConfig config = new AppConfig(httpClient, jsonUtil);
    List<ApiClient> clients = config.getApiClients();

    assertEquals(3, clients.size());
    assertTrue(clients.stream().anyMatch(c -> c.getSourceName().equals("chucknorris")));
    assertTrue(clients.stream().anyMatch(c -> c.getSourceName().equals("zippopotam")));
    assertTrue(clients.stream().anyMatch(c -> c.getSourceName().equals("randomuser")));
  }
}
