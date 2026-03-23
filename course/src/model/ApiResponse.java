package model;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

public class ApiResponse
{
  private final String source;
  private final Instant timestamp;
  private final JsonNode data;

  public ApiResponse(String source, Instant timestamp, JsonNode data)
  {
    this.source = source;
    this.timestamp = timestamp;
    this.data = data;
  }

  public String getSource()
  {
    return source;
  }

  public Instant getTimestamp()
  {
    return timestamp;
  }

  public JsonNode getData()
  {
    return data;
  }
}
