package model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.UUID;

public class AggregatedRecord
{
  private final String id;
  private final String source;
  private final Instant timestamp;
  private final JsonNode data;

  public AggregatedRecord(String source, Instant timestamp, JsonNode data)
  {
    this.id = UUID.randomUUID().toString();
    this.source = source;
    this.timestamp = timestamp;
    this.data = data;
  }

  public AggregatedRecord(String id, String source, Instant timestamp, JsonNode data)
  {
    this.id = id;
    this.source = source;
    this.timestamp = timestamp;
    this.data = data;
  }

  public String getId()
  {
    return id;
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

  public ObjectNode toJson()
  {
    ObjectNode node = JsonNodeFactory.instance.objectNode();
    node.put("id", id);
    node.put("source", source);
    node.put("timestamp", timestamp.toString());
    node.set("data", data);
    return node;
  }
}
