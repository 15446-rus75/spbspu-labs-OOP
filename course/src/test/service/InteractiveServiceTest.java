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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InteractiveServiceTest
{

  @Mock
  private ApiService apiService;
  @Mock
  private FileService fileService;
  @Mock
  private ApiClient mockClient;

  private JsonUtil jsonUtil;
  private InteractiveService interactiveService;

  @BeforeEach
  void setUp()
  {
    jsonUtil = new JsonUtil();
    interactiveService = new InteractiveService(apiService, fileService, jsonUtil);
    lenient().when(mockClient.getSourceName()).thenReturn("test");
  }

  @Test
  void fetchFromApisParallel_shouldReturnRecordsInParallel() throws Exception
  {
    when(apiService.getClient("test")).thenReturn(mockClient);
    ApiResponse response = new ApiResponse("test", Instant.now(), jsonUtil.getMapper().createObjectNode());
    when(mockClient.fetchData(any())).thenReturn(response);

    List<AggregatedRecord> records = interactiveService.fetchFromApisParallel(
      List.of("test"), new HashMap<>(), 2);

    assertEquals(1, records.size());
    assertEquals("test", records.get(0).getSource());
  }

  @Test
  void fetchFromApisParallel_shouldHandleApiException() throws Exception
  {
    when(apiService.getClient("test")).thenReturn(mockClient);
    when(mockClient.fetchData(any())).thenThrow(new ApiException("Error"));

    List<AggregatedRecord> records = interactiveService.fetchFromApisParallel(
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
    when(apiService.getClient("src")).thenReturn(mockClient);
    Map<String, Object> flat = new HashMap<>();
    when(mockClient.flattenResponse(any())).thenReturn(flat);

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
    when(apiService.getAvailableApiNames()).thenReturn(List.of("test"));
    when(apiService.getClient("test")).thenReturn(mockClient);
    ApiResponse mockResponse = new ApiResponse("test", Instant.now(),
            jsonUtil.getMapper().createObjectNode());
    when(mockClient.fetchData(any())).thenReturn(mockResponse);

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
    when(apiService.getClient("test1")).thenReturn(mockClient);
    when(apiService.getClient("test2")).thenReturn(mockClient);
    ApiResponse mockResponse = new ApiResponse("test", Instant.now(),
            jsonUtil.getMapper().createObjectNode());
    when(mockClient.fetchData(any())).thenReturn(mockResponse);

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
}
