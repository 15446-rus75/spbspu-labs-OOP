package api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.ApiException;
import model.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import util.HttpClientUtil;
import util.JsonUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiClientTest
{
  @Mock
  private HttpClientUtil httpClient;

  private JsonUtil jsonUtil;
  private ObjectMapper mapper;

  @BeforeEach
  void setUp()
  {
    jsonUtil = new JsonUtil();
    mapper = jsonUtil.getMapper();
  }

  @Test
  void chuckNorrisClient_shouldReturnCorrectSourceName()
  {
    ChuckNorrisClient client = new ChuckNorrisClient(httpClient, jsonUtil);
    assertEquals("chucknorris", client.getSourceName());
  }

  @Test
  void chuckNorrisClient_shouldBuildUrlWithoutParams()
  {
    ChuckNorrisClient client = new ChuckNorrisClient(httpClient, jsonUtil);
    Map< String, String > params = new HashMap<>();
    String url = client.buildUrl(params);
    assertEquals("https://api.chucknorris.io/jokes/random", url);
  }

  @Test
  void chuckNorrisClient_shouldBuildUrlWithParams()
  {
    ChuckNorrisClient client = new ChuckNorrisClient(httpClient, jsonUtil);
    Map< String, String > params = new HashMap<>();
    params.put("category", "animal");
    params.put("name", "Bob");
    String url = client.buildUrl(params);
    assertTrue(url.startsWith("https://api.chucknorris.io/jokes/random?"));
    assertTrue(url.contains("category=animal"));
    assertTrue(url.contains("name=Bob"));
  }

  @Test
  void chuckNorrisClient_fetchData_shouldReturnApiResponse() throws Exception
  {
    String mockJson = getChuckNorrisMockJson();
    when(httpClient.get(anyString())).thenReturn(mockJson);
    ChuckNorrisClient client = new ChuckNorrisClient(httpClient, jsonUtil);

    ApiResponse response = client.fetchData(new HashMap<>());

    assertNotNull(response);
    assertEquals("chucknorris", response.getSource());
    assertNotNull(response.getTimestamp());
    assertEquals("123", response.getData().path("id").asText());
    verify(httpClient).get(anyString());
  }

  @Test
  void chuckNorrisClient_fetchData_shouldThrowApiExceptionOnHttpError() throws Exception
  {
    when(httpClient.get(anyString())).thenThrow(new IOException("Network error"));
    ChuckNorrisClient client = new ChuckNorrisClient(httpClient, jsonUtil);

    assertThrows(ApiException.class, () -> client.fetchData(new HashMap<>()));
  }

  @Test
  void randomUserClient_shouldExtractFirstResult()
  {
    RandomUserClient client = new RandomUserClient(httpClient, jsonUtil);
    ObjectNode root = mapper.createObjectNode();
    ObjectNode user = mapper.createObjectNode();
    user.put("gender", "male");
    root.set("results", mapper.createArrayNode().add(user));

    JsonNode extracted = client.extractData(root);

    assertEquals("male", extracted.path("gender").asText());
  }

  @Test
  void zipCodeClient_shouldBuildUrlWithZipParam()
  {
    ZipCodeClient client = new ZipCodeClient(httpClient, jsonUtil);
    Map< String, String > params = new HashMap<>();
    params.put("zip", "12345");
    String url = client.buildUrl(params);
    assertEquals("http://api.zippopotam.us/us/12345", url);
  }

  @Test
  void zipCodeClient_shouldUseDefaultZipIfNotProvided()
  {
    ZipCodeClient client = new ZipCodeClient(httpClient, jsonUtil);
    String url = client.buildUrl(new HashMap<>());
    assertEquals("http://api.zippopotam.us/us/90210", url);
  }

  @Test
  void zipCodeClient_fetchData_shouldUseDefaultZipWhenNoParams() throws Exception
  {
    String mockJson = getZipCodeMockJson();
    when(httpClient.get(contains("90210"))).thenReturn(mockJson);
    ZipCodeClient client = new ZipCodeClient(httpClient, jsonUtil);

    ApiResponse response = client.fetchData(new HashMap<>());

    assertEquals("zippopotam", response.getSource());
    assertEquals("90210", response.getData().path("post code").asText());
  }

  @Test
  void zipCodeClient_flattenResponse_shouldHandleMissingPlaces() throws Exception
  {
    String mockJson = "{ \"post code\": \"90210\", \"country\": \"United States\", \"country abbreviation\": \"US\", \"places\": []}";
    when(httpClient.get(anyString())).thenReturn(mockJson);
    ZipCodeClient client = new ZipCodeClient(httpClient, jsonUtil);

    ApiResponse response = client.fetchData(new HashMap<>());
    Map< String, Object > flat = client.flattenResponse(response);

    assertEquals("90210", flat.get("post_code"));
    assertEquals("United States", flat.get("country"));
    assertNull(flat.get("place_name"));
  }

  @Test
  void allClients_unflatten_shouldRestoreJsonStructure() throws Exception
  {
    testUnflattenForClient(new ChuckNorrisClient(httpClient, jsonUtil), "chucknorris", "id");
    testUnflattenForClient(new RandomUserClient(httpClient, jsonUtil), "randomuser", "gender");
    testUnflattenForClient(new ZipCodeClient(httpClient, jsonUtil), "zippopotam", "post code");
  }

  private void testUnflattenForClient(ApiClient client, String source, String expectedField) throws Exception
  {
    String mockJson = getMockJsonForClient(source);
    when(httpClient.get(anyString())).thenReturn(mockJson);

    ApiResponse response = client.fetchData(new HashMap<>());
    Map<String, Object> flat = client.flattenResponse(response);
    JsonNode restored = client.unflatten(flat);

    assertNotNull(restored);
    assertEquals(response.getData().path(expectedField).asText(), restored.path(expectedField).asText());
  }

  private String getMockJsonForClient(String source)
  {
    if ("chucknorris".equals(source))
    {
      return getChuckNorrisMockJson();
    }
    else if ("randomuser".equals(source))
    {
      return "{ \"results\":[{ \"gender\": \"male\", \"name\":{ \"title\": \"Mr\", \"first\": \"John\", \"last\": \"Doe\"}, \"email\": \"john@example.com\", \"location\":{ \"country\": \"USA\", \"city\": \"New York\"}, \"phone\": \"123456\"}]}";
    }
    else if ("zippopotam".equals(source))
    {
      return getZipCodeMockJson();
    }
    return "{}";
  }

  private String getChuckNorrisMockJson()
  {
    return "{ \"id\": \"123\", \"value\": \"Chuck Norris joke\", \"created_at\": \"2020-01-01\", \"categories\":[], \"icon_url\": \"url\", \"updated_at\": \"2020-01-01\"}";
  }

  private String getZipCodeMockJson()
  {
    return "{ \"post code\": \"90210\", \"country\": \"United States\", \"country abbreviation\": \"US\", \"places\": [{ \"place name\": \"Beverly Hills\", \"state\": \"California\", \"state abbreviation\": \"CA\", \"latitude\": \"34.0901\", \"longitude\": \"-118.4065\"}]}";
  }
}
