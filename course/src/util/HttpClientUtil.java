package util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ConnectionSpec;
import okhttp3.TlsVersion;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class HttpClientUtil
{
  private final OkHttpClient client;

  public HttpClientUtil()
  {
    ConnectionSpec connectionSpec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
        .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_3)
        .build();

    this.client = new OkHttpClient.Builder()
        .connectionSpecs(Arrays.asList(connectionSpec, ConnectionSpec.CLEARTEXT))
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build();

    System.out.println("HttpClientUtil инициализирован: connectTimeout=60s, readTimeout=120s, writeTimeout=120s, HTTP/2 отключён");
  }

  public String get(String url) throws IOException
  {
    Request request = new Request.Builder()
        .url(url)
        .header("User-Agent", "curl/7.81.0")
        .build();
    try (Response response = client.newCall(request).execute())
    {
      if (!response.isSuccessful())
      {
        throw new IOException("Unexpected HTTP status code: " + response.code());
      }
      return response.body().string();
    }
    catch (IOException e)
    {
      System.err.println("Ошибка запроса к " + url + ": " + e.getClass().getSimpleName() + ": " + e.getMessage());
      throw e;
    }
  }
}
