package service;

import api.ApiClient;
import exception.ApiException;
import exception.FileProcessingException;
import model.ApiResponse;
import model.AggregatedRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import util.JsonUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InteractiveServiceTest
{
  @Mock
  private ApiService apiService;

  @Mock
  private FileService fileService;

  private JsonUtil jsonUtil;
  private InteractiveService interactiveService;

  @BeforeEach
  void setUp()
  {
    jsonUtil = new JsonUtil();
    interactiveService = new InteractiveService(apiService, fileService, jsonUtil);
  }

  @Test
  void fetchFromApisParallel_shouldReturnRecordsInParallel() throws Exception
  {
    ApiClient mockClient = setupMockClient("test");
    when(apiService.getClient("test")).thenReturn(mockClient);

    List< AggregatedRecord > records = interactiveService.fetchFromApisParallel(
      List.of("test"), new HashMap<>(), 2);

    assertEquals(1, records.size());
    assertEquals("test", records.get(0).getSource());
  }

  @Test
  void fetchFromApisParallel_shouldHandleApiException() throws Exception
  {
    ApiClient mockClient = mock(ApiClient.class);
    lenient().when(mockClient.getSourceName()).thenReturn("test");
    when(apiService.getClient("test")).thenReturn(mockClient);
    when(mockClient.fetchData(any())).thenThrow(new ApiException("Error"));

    List< AggregatedRecord > records = interactiveService.fetchFromApisParallel(
      List.of("test"), new HashMap<>(), 2);

    assertTrue(records.isEmpty());
  }

  @Test
  void fetchFromApisParallel_withNullClient_shouldSkip() throws Exception
  {
    when(apiService.getClient("unknown")).thenReturn(null);

    List< AggregatedRecord > records = interactiveService.fetchFromApisParallel(
      List.of("unknown"), new HashMap<>(), 2);

    assertTrue(records.isEmpty());
  }

  @Test
  void saveRecords_json_shouldDelegateToFileService() throws Exception
  {
    AggregatedRecord record = new AggregatedRecord("src", Instant.now(), jsonUtil.getMapper().createObjectNode());
    interactiveService.saveRecords(List.of(record), "file.json", "json", false);

    verify(fileService).saveRecordsAsJson(anyList(), any(), eq(false));
  }

  @Test
  void saveRecords_csv_shouldFlattenAndSave() throws Exception
  {
    ApiClient mockClient = setupMockClient("src");
    when(apiService.getClient("src")).thenReturn(mockClient);
    Map< String, Object > flat = new HashMap<>();
    lenient().when(mockClient.flattenResponse(any())).thenReturn(flat);

    AggregatedRecord record = new AggregatedRecord("src", Instant.now(), jsonUtil.getMapper().createObjectNode());
    interactiveService.saveRecords(List.of(record), "file.csv", "csv", true);

    verify(fileService).saveRecordsAsCsv(anyList(), any(), eq(true));
  }

  @Test
  void saveRecords_csv_withNullClient_shouldSkip() throws Exception
  {
    when(apiService.getClient("unknown")).thenReturn(null);

    AggregatedRecord record = new AggregatedRecord("unknown", Instant.now(), jsonUtil.getMapper().createObjectNode());
    interactiveService.saveRecords(List.of(record), "file.csv", "csv", false);

    verify(fileService).saveRecordsAsCsv(anyList(), any(), eq(false));
  }

  @Test
  void saveRecords_unsupportedFormat_shouldThrowException()
  {
    AggregatedRecord record = new AggregatedRecord("src", Instant.now(), jsonUtil.getMapper().createObjectNode());
    assertThrows(FileProcessingException.class, () ->
      interactiveService.saveRecords(List.of(record), "file.txt", "txt", false));
  }

  @Test
  void runAutoMode_withInterval_shouldStartPollingAndStopOnEnter() throws Exception
  {
    setupAutoModeMocks("test");
    InputStream originalIn = System.in;
    System.setIn(new ByteArrayInputStream("\n".getBytes()));
    try
    {
      interactiveService.runAutoMode("test", "json", "out.json", 2, 1);
    }
    finally
    {
      System.setIn(originalIn);
    }

    verify(fileService, atLeastOnce()).saveRecordsAsJson(anyList(), any(), eq(true));
  }

  @Test
  void runAutoMode_allApis_shouldUseAllAvailableApis() throws Exception
  {
    when(apiService.getAvailableApiNames()).thenReturn(List.of("test1", "test2"));
    ApiClient client1 = setupMockClient("test1");
    ApiClient client2 = setupMockClient("test2");
    when(apiService.getClient("test1")).thenReturn(client1);
    when(apiService.getClient("test2")).thenReturn(client2);

    interactiveService.runAutoMode("all", "json", "out.json", 2, 0);

    verify(fileService).saveRecordsAsJson(anyList(), any(), eq(false));
  }

  @Test
  void runAutoMode_emptyApiList_shouldPrintMessage() throws Exception
  {
    when(apiService.getAvailableApiNames()).thenReturn(List.of("test"));
    interactiveService.runAutoMode("unknown", "json", "out.json", 2, 0);

    verify(fileService, never()).saveRecordsAsJson(anyList(), any(), anyBoolean());
  }

  @Test
  void runAutoMode_withEmptyRecords_shouldPrintMessage() throws Exception
  {
    when(apiService.getAvailableApiNames()).thenReturn(List.of("test"));
    ApiClient mockClient = mock(ApiClient.class);
    lenient().when(mockClient.getSourceName()).thenReturn("test");
    when(apiService.getClient("test")).thenReturn(mockClient);
    when(mockClient.fetchData(any())).thenThrow(new ApiException("Error"));

    interactiveService.runAutoMode("test", "json", "out.json", 2, 0);

    verify(fileService, never()).saveRecordsAsJson(anyList(), any(), anyBoolean());
  }

  @Test
  void runAutoMode_withFileProcessingException_shouldHandleGracefully() throws Exception
  {
    setupAutoModeMocks("test");
    org.mockito.Mockito.doThrow(new FileProcessingException("Disk full"))
      .when(fileService).saveRecordsAsJson(anyList(), any(), anyBoolean());

    assertDoesNotThrow(() -> interactiveService.runAutoMode("test", "json", "out.json", 2, 0));
  }

  @Test
  void displayRecords_json_shouldReadAndPrint() throws Exception
  {
    AggregatedRecord record = new AggregatedRecord("test", Instant.now(), jsonUtil.getMapper().createObjectNode());
    when(fileService.readRecords("file.json", "json")).thenReturn(List.of(record));

    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    try
    {
      interactiveService.displayRecords("file.json", "json", null);
    }
    finally
    {
      System.setOut(originalOut);
    }

    String output = outContent.toString();
    assertTrue(output.contains("test"));
  }

  @Test
  void displayRecords_csv_shouldReadAndPrint() throws Exception
  {
    AggregatedRecord record = new AggregatedRecord("test", Instant.now(), jsonUtil.getMapper().createObjectNode());
    when(fileService.readCsvRecords(any(Path.class), any(ApiService.class))).thenReturn(List.of(record));

    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    try
    {
      interactiveService.displayRecords("file.csv", "csv", "test");
    }
    finally
    {
      System.setOut(originalOut);
    }

    String output = outContent.toString();
    assertTrue(output.contains("test"));
  }

  @Test
  void displayRecords_unsupportedFormat_shouldPrintError() throws Exception
  {
    ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    PrintStream originalErr = System.err;
    System.setErr(new PrintStream(errContent));
    try
    {
      interactiveService.displayRecords("file.txt", "txt", null);
    }
    finally
    {
      System.setErr(originalErr);
    }

    String output = errContent.toString();
    assertTrue(output.contains("Неподдерживаемый формат"));
  }

  @Test
  void displayRecords_withException_shouldHandleGracefully() throws Exception
  {
    when(fileService.readRecords("file.json", "json")).thenThrow(new FileProcessingException("Read error"));

    ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    PrintStream originalErr = System.err;
    System.setErr(new PrintStream(errContent));
    try
    {
      interactiveService.displayRecords("file.json", "json", null);
    }
    finally
    {
      System.setErr(originalErr);
    }

    String output = errContent.toString();
    assertTrue(output.contains("Ошибка чтения файла"));
  }

  @Test
  void fetchFromApis_shouldDelegateToApiService() throws Exception
  {
    AggregatedRecord expectedRecord = new AggregatedRecord("test", Instant.now(), jsonUtil.getMapper().createObjectNode());
    when(apiService.fetchDataFromApis(anyList(), anyMap())).thenReturn(List.of(expectedRecord));

    List< AggregatedRecord > records = interactiveService.fetchFromApis(List.of("test"), new HashMap<>());

    assertEquals(1, records.size());
    verify(apiService).fetchDataFromApis(List.of("test"), new HashMap<>());
  }

  private ApiClient setupMockClient(String apiName) throws Exception
  {
    ApiClient client = mock(ApiClient.class);
    lenient().when(client.getSourceName()).thenReturn(apiName);
    lenient().when(apiService.getClient(apiName)).thenReturn(client);
    ApiResponse mockResponse = new ApiResponse(apiName, Instant.now(),
      jsonUtil.getMapper().createObjectNode());
    lenient().when(client.fetchData(any())).thenReturn(mockResponse);
    return client;
  }

  private void setupAutoModeMocks(String apiName) throws Exception
  {
    when(apiService.getAvailableApiNames()).thenReturn(List.of(apiName));
    ApiClient client = setupMockClient(apiName);
    ApiResponse mockResponse = new ApiResponse(apiName, Instant.now(),
      jsonUtil.getMapper().createObjectNode());
    lenient().when(client.fetchData(any())).thenReturn(mockResponse);
  }
}
