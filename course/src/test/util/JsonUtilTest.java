package util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilTest
{

  private JsonUtil jsonUtil;

  @BeforeEach
  void setUp()
  {
    jsonUtil = new JsonUtil();
  }

  @Test
  void parse_validJson_shouldReturnJsonNode() throws IOException
  {
    String json = "{\"key\":\"value\"}";
    JsonNode node = jsonUtil.parse(json);
    assertEquals("value", node.path("key").asText());
  }

  @Test
  void parse_invalidJson_shouldThrowIOException()
  {
    String invalid = "{not json}";
    assertThrows(IOException.class, () -> jsonUtil.parse(invalid));
  }

  @Test
  void writeAndReadArray_shouldPreserveData(@TempDir Path tempDir) throws IOException
  {
    Path file = tempDir.resolve("array.json");
    ObjectNode obj1 = jsonUtil.getMapper().createObjectNode().put("id", 1);
    ObjectNode obj2 = jsonUtil.getMapper().createObjectNode().put("id", 2);
    List<ObjectNode> list = List.of(obj1, obj2);

    jsonUtil.write(file, list);
    ArrayNode read = jsonUtil.readArray(file);

    assertEquals(2, read.size());
    assertEquals(1, read.get(0).path("id").asInt());
    assertEquals(2, read.get(1).path("id").asInt());
  }

  @Test
  void getMapper_shouldReturnConfiguredObjectMapper()
  {
    assertNotNull(jsonUtil.getMapper());
  }
}
