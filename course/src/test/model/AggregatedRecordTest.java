package model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AggregatedRecordTest
{

  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void constructorWithAutoId_shouldGenerateUuid()
  {
    ObjectNode data = mapper.createObjectNode();
    AggregatedRecord record = new AggregatedRecord("src", Instant.now(), data);
    assertNotNull(record.getId());
    assertFalse(record.getId().isEmpty());
  }

  @Test
  void constructorWithGivenId_shouldUseProvidedId()
  {
    ObjectNode data = mapper.createObjectNode();
    String customId = "custom-id";
    AggregatedRecord record = new AggregatedRecord(customId, "src", Instant.now(), data);
    assertEquals(customId, record.getId());
  }

  @Test
  void toJson_shouldIncludeAllFields()
  {
    ObjectNode data = mapper.createObjectNode().put("field", "value");
    Instant now = Instant.now();
    AggregatedRecord record = new AggregatedRecord("id123", "source", now, data);

    ObjectNode json = record.toJson();
    assertEquals("id123", json.path("id").asText());
    assertEquals("source", json.path("source").asText());
    assertEquals(now.toString(), json.path("timestamp").asText());
    assertEquals("value", json.path("data").path("field").asText());
  }

  @Test
  void getters_shouldReturnCorrectValues()
  {
    ObjectNode data = mapper.createObjectNode();
    Instant now = Instant.now();
    AggregatedRecord record = new AggregatedRecord("id", "src", now, data);

    assertEquals("id", record.getId());
    assertEquals("src", record.getSource());
    assertEquals(now, record.getTimestamp());
    assertSame(data, record.getData());
  }
}
