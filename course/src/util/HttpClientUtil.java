package util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

public class HttpClientUtil
{
  private final OkHttpClient client;

  public HttpClientUtil()
  {
    this.client = new OkHttpClient();
  }

  public String get(String url) throws IOException
  {
    Request request = new Request.Builder().url(url).build();
    try (Response response = client.newCall(request).execute())
    {
      if (!response.isSuccessful())
      {
        throw new IOException("Unexpected code " + response);
      }
      return response.body().string();
    }
  }
}
