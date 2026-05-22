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
import java.io.InputStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
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
