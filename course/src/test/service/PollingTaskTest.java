package service;

import api.ApiClient;
import exception.ApiException;
import model.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import util.JsonUtil;

import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PollingTaskTest
{

  @Mock
  private ApiClient client;
  @Mock
  private FileService fileService;
  @Mock
  private ApiService apiService;

  private final JsonUtil jsonUtil = new JsonUtil();

  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp()
  {
    when(client.getSourceName()).thenReturn("testApi");
  }

  @Test
  void run_shouldFetchDataAndSaveAsJson() throws Exception
  {
    ApiResponse response = new ApiResponse("testApi", Instant.now(), jsonUtil.getMapper().createObjectNode());
    when(client.fetchData(any())).thenReturn(response);

    PollingTask task = new PollingTask(client, new HashMap<>(), "json",
      tempDir.resolve("out.json").toString(), true, fileService, apiService);
    task.run();

    verify(fileService).saveRecordsAsJson(anyList(), any(Path.class), eq(true));
  }

  @Test
  void run_shouldFetchDataAndSaveAsCsv() throws Exception
  {
    ApiResponse response = new ApiResponse("testApi", Instant.now(), jsonUtil.getMapper().createObjectNode());
    when(client.fetchData(any())).thenReturn(response);
    Map<String, Object> flat = new HashMap<>();
    flat.put("field", "value");
    when(client.flattenResponse(any())).thenReturn(flat);

    PollingTask task = new PollingTask(client, new HashMap<>(), "csv",
      tempDir.resolve("out.csv").toString(), true, fileService, apiService);
    task.run();

    verify(fileService).saveRecordsAsCsv(anyList(), any(Path.class), eq(true));
  }

  @Test
  void run_shouldHandleApiException() throws Exception
  {
    when(client.fetchData(any())).thenThrow(new ApiException("Error"));

    PollingTask task = new PollingTask(client, new HashMap<>(), "json",
      tempDir.resolve("out.json").toString(), true, fileService, apiService);
    task.run();

    verify(fileService, never()).saveRecordsAsJson(anyList(), any(), anyBoolean());
  }

  @Test
  void run_shouldHandleInterruptedException() throws Exception
  {
    when(client.fetchData(any())).thenThrow(new RuntimeException(new InterruptedException()));

    PollingTask task = new PollingTask(client, new HashMap<>(), "json",
      tempDir.resolve("out.json").toString(), true, fileService, apiService);
    task.run();

    assertTrue(Thread.interrupted());
  }

  @Test
  void run_whenInterruptedBeforeRun_shouldNotFetchData() throws Exception
  {
    Thread.currentThread().interrupt();
    PollingTask task = new PollingTask(client, new HashMap<>(), "json",
            tempDir.resolve("out.json").toString(), true, fileService, apiService);
    task.run();

    verify(client, never()).fetchData(any());
    assertTrue(Thread.interrupted());
  }
}
