package util;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class HttpClientUtilTest
{

  private MockWebServer mockWebServer;
  private HttpClientUtil httpClientUtil;

  @BeforeEach
  void setUp() throws IOException
  {
    mockWebServer = new MockWebServer();
    mockWebServer.start();
    httpClientUtil = new HttpClientUtil();
  }

  @AfterEach
  void tearDown() throws IOException
  {
    mockWebServer.shutdown();
  }

  @Test
  void get_shouldReturnResponseBody() throws IOException
  {
    String responseBody = "{\"message\":\"success\"}";
    mockWebServer.enqueue(new MockResponse()
      .setBody(responseBody)
      .setResponseCode(200));

    String url = mockWebServer.url("/test").toString();
    String result = httpClientUtil.get(url);

    assertEquals(responseBody, result);
  }

  @Test
  void get_shouldThrowIOExceptionOnNonSuccessfulResponse()
  {
    mockWebServer.enqueue(new MockResponse().setResponseCode(404));

    String url = mockWebServer.url("/notfound").toString();
    assertThrows(IOException.class, () -> httpClientUtil.get(url));
  }

  @Test
  void get_shouldPropagateIOException() throws IOException
  {
    mockWebServer.shutdown();
    String invalidUrl = "http://localhost:1";
    assertThrows(IOException.class, () -> httpClientUtil.get(invalidUrl));
  }
}
