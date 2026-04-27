package service;

import api.ApiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.FileProcessingException;
import model.AggregatedRecord;
import model.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import util.JsonUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileServiceTest
{

  @TempDir
  Path tempDir;

  private FileService fileService;
  private JsonUtil jsonUtil;
  private ObjectMapper mapper;
  private ApiService apiServiceMock;

  @BeforeEach
  void setUp()
  {
    jsonUtil = new JsonUtil();
    mapper = jsonUtil.getMapper();
    fileService = new FileService(jsonUtil);
    apiServiceMock = mock(ApiService.class);
  }

  @Test
  void saveAndReadJson_shouldPreserveData() throws Exception
  {
    Path file = tempDir.resolve("test.json");
    ObjectNode data = mapper.createObjectNode().put("field", "value");
    AggregatedRecord record = new AggregatedRecord("testSource", Instant.now(), data);

    fileService.saveRecordsAsJson(List.of(record), file, false);
    List<AggregatedRecord> read = fileService.readRecords(file.toString(), "json");

    assertEquals(1, read.size());
    AggregatedRecord loaded = read.get(0);
    assertEquals("testSource", loaded.getSource());
    assertEquals("value", loaded.getData().path("field").asText());
  }

  @Test
  void saveJsonAppend_shouldAddToExistingFile() throws Exception
  {
    Path file = tempDir.resolve("test_append.json");
    ObjectNode data1 = mapper.createObjectNode().put("id", 1);
    ObjectNode data2 = mapper.createObjectNode().put("id", 2);
    AggregatedRecord record1 = new AggregatedRecord("src1", Instant.now(), data1);
    AggregatedRecord record2 = new AggregatedRecord("src2", Instant.now(), data2);

    fileService.saveRecordsAsJson(List.of(record1), file, false);
    fileService.saveRecordsAsJson(List.of(record2), file, true);

    List<AggregatedRecord> read = fileService.readRecords(file.toString(), "json");
    assertEquals(2, read.size());
    assertEquals(1, read.get(0).getData().path("id").asInt());
    assertEquals(2, read.get(1).getData().path("id").asInt());
  }

  @Test
  void saveCsvAndReadViaApiService_shouldRestoreData() throws Exception
  {
    Path file = tempDir.resolve("test.csv");

    ApiClient mockClient = mock(ApiClient.class);
    when(mockClient.getSourceName()).thenReturn("testApi");
    when(apiServiceMock.getClient("testApi")).thenReturn(mockClient);

    ObjectNode originalData = mapper.createObjectNode().put("value", "test");
    AggregatedRecord record = new AggregatedRecord("testApi", Instant.now(), originalData);

    Map<String, Object> flatMap = new LinkedHashMap<>();
    flatMap.put("custom_field", "test_value");
    when(mockClient.flattenResponse(any(ApiResponse.class))).thenReturn(flatMap);
    when(mockClient.unflatten(any())).thenReturn(originalData);

    Map<String, Object> flat = new LinkedHashMap<>(flatMap);
    flat.put("id", record.getId());
    flat.put("source", record.getSource());
    flat.put("timestamp", record.getTimestamp().toString());
    fileService.saveRecordsAsCsv(List.of(flat), file, false);

    List<AggregatedRecord> read = fileService.readCsvRecords(file, apiServiceMock);
    assertEquals(1, read.size());
    AggregatedRecord loaded = read.get(0);
    assertEquals("testApi", loaded.getSource());
    assertEquals("test", loaded.getData().path("value").asText());
  }

  @Test
  void readCsvRecords_withDataColumn_shouldParseCorrectly() throws Exception
  {
    Path file = tempDir.resolve("test_with_data.csv");
    String csvContent = "id,source,timestamp,data\n"
            + "1,testApi,2024-01-01T00:00:00Z,\"{\"\"value\"\":\"\"test\"\"}\"\n";
    Files.writeString(file, csvContent);

    ApiClient mockClient = mock(ApiClient.class);
    when(mockClient.getSourceName()).thenReturn("testApi");
    when(apiServiceMock.getClient("testApi")).thenReturn(mockClient);

    List<AggregatedRecord> records = fileService.readCsvRecords(file, apiServiceMock);
    assertEquals(1, records.size());
    AggregatedRecord rec = records.get(0);
    assertEquals("testApi", rec.getSource());
    assertEquals("test", rec.getData().path("value").asText());
  }

  @Test
  void readRecords_shouldReturnEmptyListForNonExistentFile() throws Exception
  {
    List<AggregatedRecord> records = fileService.readRecords("nonexistent.json", "json");
    assertTrue(records.isEmpty());
  }

  @Test
  void readRecords_unsupportedFormat_shouldThrowException() throws Exception
  {
    Path file = tempDir.resolve("file.txt");
    Files.writeString(file, "dummy content");

    assertThrows(FileProcessingException.class, () ->
      fileService.readRecords(file.toString(), "txt"));
  }

  @Test
  void saveRecordsAsJson_emptyList_shouldDoNothing() throws Exception
  {
    Path file = tempDir.resolve("empty.json");
    fileService.saveRecordsAsJson(Collections.emptyList(), file, false);
    assertFalse(Files.exists(file));
  }

  @Test
  void saveRecordsAsCsv_emptyList_shouldDoNothing() throws Exception
  {
    Path file = tempDir.resolve("empty.csv");
    fileService.saveRecordsAsCsv(Collections.emptyList(), file, false);
    assertFalse(Files.exists(file));
  }
}
