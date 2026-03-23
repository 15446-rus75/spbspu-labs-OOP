package util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class JsonUtil
{
  private final ObjectMapper mapper;

  public JsonUtil()
  {
    this.mapper = new ObjectMapper();
    this.mapper.registerModule(new JavaTimeModule());
  }

  public ObjectMapper getMapper()
  {
    return mapper;
  }

  public JsonNode parse(String json) throws IOException
  {
    return mapper.readTree(json);
  }

  public ArrayNode readArray(Path path) throws IOException
  {
    return mapper.readValue(path.toFile(), ArrayNode.class);
  }

  public void write(Path path, List<?> objects) throws IOException
  {
    mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), objects);
  }
}
