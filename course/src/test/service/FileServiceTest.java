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

    List< AggregatedRecord > read = fileService.readRecords(file.toString(), "json");
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

    List< AggregatedRecord > read = fileService.readRecords(file.toString(), "json");
    assertEquals(2, read.size());
    assertEquals(1, read.get(0).getData().path("id").asInt());
    assertEquals(2, read.get(1).getData().path("id").asInt());
  }

  @Test
  void saveCsvAndReadViaApiService_shouldRestoreData() throws Exception
  {
    Path file = tempDir.resolve("test.csv");
    ApiClient mockClient = setupMockClient("testApi");
    ObjectNode originalData = mapper.createObjectNode().put("value", "test");
    AggregatedRecord record = new AggregatedRecord("testApi", Instant.now(), originalData);

    Map< String, Object > flatMap = createFlatMap();
    when(mockClient.flattenResponse(any(ApiResponse.class))).thenReturn(flatMap);
    when(mockClient.unflatten(any())).thenReturn(originalData);

    Map< String, Object > flat = new LinkedHashMap<>(flatMap);
    flat.put("id", record.getId());
    flat.put("source", record.getSource());
    flat.put("timestamp", record.getTimestamp().toString());

    fileService.saveRecordsAsCsv(List.of(flat), file, false);

    List< AggregatedRecord > read = fileService.readCsvRecords(file, apiServiceMock);
    assertEquals(1, read.size());
    AggregatedRecord loaded = read.get(0);
    assertEquals("testApi", loaded.getSource());
    assertEquals("test", loaded.getData().path("value").asText());
  }

  @Test
  void readCsvRecords_withDataColumn_shouldParseCorrectly() throws Exception
  {
    Path file = tempDir.resolve("test_with_data.csv");
    String csvContent = "id,source,timestamp,data\n" +
      "1,testApi,2024-01-01T00:00:00Z,\"{\"value\":\"test\"}\"\n";
    Files.writeString(file, csvContent);
    setupMockClient("testApi");
    List< AggregatedRecord > records = fileService.readCsvRecords(file, apiServiceMock);
    assertEquals(1, records.size());
    AggregatedRecord rec = records.get(0);
    assertEquals("testApi", rec.getSource());
    assertEquals("test", rec.getData().path("value").asText());
  }

  @Test
  void readRecords_shouldReturnEmptyListForNonExistentFile() throws Exception
  {
    List< AggregatedRecord > records = fileService.readRecords("nonexistent.json", "json");
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

  @Test
  void saveRecordsAsCsv_appendToEmptyFile_shouldHandleNullHeader() throws Exception
  {
    Path file = tempDir.resolve("empty_then_append.csv");
    Files.writeString(file, "");

    Map< String, Object > flat = new LinkedHashMap<>();
    flat.put("id", "1");
    flat.put("source", "test");
    flat.put("timestamp", "2024-01-01T00:00:00Z");
    flat.put("field", "value");

    assertThrows(FileProcessingException.class, () ->
      fileService.saveRecordsAsCsv(List.of(flat), file, true));
  }

  @Test
  void readCsvWithDataColumn_missingRequiredColumns_shouldReturnEmptyList() throws Exception
  {
    Path file = tempDir.resolve("missing_columns.csv");
    String csvContent = "id,source,data\n" +
      "1,testApi,\"{\"value\":\"test\"}\"\n";
    Files.writeString(file, csvContent);

    List< AggregatedRecord > records = fileService.readCsvRecords(file, apiServiceMock);
    assertTrue(records.isEmpty());
  }

  @Test
  void readCsvWithDataColumn_insufficientColumnsInRow_shouldSkipRow() throws Exception
  {
    Path file = tempDir.resolve("insufficient_columns.csv");
    String csvContent = "id,source,timestamp,data\n" +
      "1,testApi\n" +
      "2,testApi,2024-01-01T00:00:00Z,\"{\"value\":\"test\"}\"\n";
    Files.writeString(file, csvContent);

    List< AggregatedRecord > records = fileService.readCsvRecords(file, apiServiceMock);
    assertEquals(1, records.size());
  }

  @Test
  void readCsvWithDataColumn_invalidTimestamp_shouldSkipRow() throws Exception
  {
    Path file = tempDir.resolve("invalid_timestamp.csv");
    String csvContent = "id,source,timestamp,data\n" +
      "1,testApi,invalid-date,\"{\"value\":\"test\"}\"\n" +
      "2,testApi,2024-01-01T00:00:00Z,\"{\"value\":\"test2\"}\"\n";
    Files.writeString(file, csvContent);

    List< AggregatedRecord > records = fileService.readCsvRecords(file, apiServiceMock);
    assertEquals(1, records.size());
    assertEquals("2", records.get(0).getId());
  }

  @Test
  void readCsvWithDataColumn_invalidJson_shouldSkipRow() throws Exception
  {
    Path file = tempDir.resolve("invalid_json.csv");
    String csvContent = "id,source,timestamp,data\n" +
      "1,testApi,2024-01-01T00:00:00Z,\"not valid json\"\n" +
      "2,testApi,2024-01-01T00:00:00Z,\"{\"value\":\"test\"}\"\n";
    Files.writeString(file, csvContent);

    List< AggregatedRecord > records = fileService.readCsvRecords(file, apiServiceMock);
    assertEquals(1, records.size());
    assertEquals("2", records.get(0).getId());
  }

  @Test
  void readCsvWithFlattenedColumns_missingRequiredColumns_shouldReturnEmptyList() throws Exception
  {
    Path file = tempDir.resolve("flattened_missing_columns.csv");
    String csvContent = "id,source,field1,field2\n" +
      "1,testApi,value1,value2\n";
    Files.writeString(file, csvContent);

    List< AggregatedRecord > records = fileService.readCsvRecords(file, apiServiceMock);
    assertTrue(records.isEmpty());
  }

  @Test
  void readCsvWithFlattenedColumns_insufficientColumnsInRow_shouldSkipRow() throws Exception
  {
    Path file = tempDir.resolve("flattened_insufficient.csv");
    String csvContent = "id,source,timestamp,field1\n" +
      "1,testApi\n" +
      "2,testApi,2024-01-01T00:00:00Z,value1\n";
    Files.writeString(file, csvContent);
    setupMockClient("testApi"); // <-- ИСПРАВЛЕНИЕ: добавлен мок для testApi

    List< AggregatedRecord > records = fileService.readCsvRecords(file, apiServiceMock);
    assertEquals(1, records.size());
  }

  @Test
  void readCsvWithFlattenedColumns_invalidTimestamp_shouldSkipRow() throws Exception
  {
    Path file = tempDir.resolve("flattened_invalid_timestamp.csv");
    String csvContent = "id,source,timestamp,field1\n" +
      "1,testApi,invalid-date,value1\n" +
      "2,testApi,2024-01-01T00:00:00Z,value2\n";
    Files.writeString(file, csvContent);
    setupMockClient("testApi"); // <-- ИСПРАВЛЕНИЕ: добавлен мок для testApi

    List< AggregatedRecord > records = fileService.readCsvRecords(file, apiServiceMock);
    assertEquals(1, records.size());
    assertEquals("2", records.get(0).getId());
  }

  @Test
  void readCsvWithFlattenedColumns_unknownSource_shouldSkipRow() throws Exception
  {
    Path file = tempDir.resolve("flattened_unknown_source.csv");
    String csvContent = "id,source,timestamp,field1\n" +
      "1,unknownApi,2024-01-01T00:00:00Z,value1\n" +
      "2,testApi,2024-01-01T00:00:00Z,value2\n";
    Files.writeString(file, csvContent);
    setupMockClient("testApi");

    List< AggregatedRecord > records = fileService.readCsvRecords(file, apiServiceMock);
    assertEquals(1, records.size());
    assertEquals("2", records.get(0).getId());
  }

  @Test
  void readCsvWithFlattenedColumns_unflattenError_shouldSkipRow() throws Exception
  {
    Path file = tempDir.resolve("flattened_unflatten_error.csv");
    String csvContent = "id,source,timestamp,field1\n" +
      "1,testApi,2024-01-01T00:00:00Z,value1\n" +
      "2,testApi,2024-01-01T00:00:00Z,value2\n";
    Files.writeString(file, csvContent);

    ApiClient mockClient = setupMockClient("testApi");
    when(mockClient.unflatten(any())).thenThrow(new RuntimeException("Unflatten error"));

    List< AggregatedRecord > records = fileService.readCsvRecords(file, apiServiceMock);
    assertTrue(records.isEmpty());
  }

  @Test
  void readRawCsv_shouldReturnAllLines() throws Exception
  {
    Path file = tempDir.resolve("raw.csv");
    String csvContent = "id,source,timestamp\n" +
      "1,testApi,2024-01-01T00:00:00Z\n" +
      "2,testApi,2024-01-02T00:00:00Z\n";
    Files.writeString(file, csvContent);

    List< String[] > lines = fileService.readRawCsv(file);
    assertEquals(3, lines.size());
    assertArrayEquals(new String[]{"id", "source", "timestamp"}, lines.get(0));
  }

  @Test
  void readRawCsv_nonExistentFile_shouldThrowException() throws Exception
  {
    Path file = tempDir.resolve("nonexistent.csv");
    assertThrows(FileProcessingException.class, () ->
      fileService.readRawCsv(file));
  }

  private ApiClient setupMockClient(String apiName)
  {
    ApiClient mockClient = mock(ApiClient.class);
    when(mockClient.getSourceName()).thenReturn(apiName);
    when(apiServiceMock.getClient(apiName)).thenReturn(mockClient);
    return mockClient;
  }

  private Map< String, Object > createFlatMap()
  {
    Map< String, Object > flatMap = new LinkedHashMap<>();
    flatMap.put("custom_field", "test_value");
    return flatMap;
  }
}
