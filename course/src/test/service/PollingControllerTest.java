package service;

import api.ApiClient;
import exception.FileProcessingException;
import model.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import util.JsonUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PollingControllerTest
{
  @Mock
  private ApiService apiService;

  @Mock
  private FileService fileService;

  @Mock
  private ApiClient mockClient;

  private PollingController controller;

  @BeforeEach
  void setUp()
  {
    controller = new PollingController(apiService, fileService);
  }

  @Test
  void defaultSettings_shouldBeFiveThreadsZeroInterval()
  {
    assertEquals(5, controller.getMaxThreads());
    assertEquals(0, controller.getInterval());
  }

  @Test
  void setMaxThreads_shouldUpdateValue()
  {
    controller.setMaxThreads(10);
    assertEquals(10, controller.getMaxThreads());
  }

  @Test
  void setInterval_shouldUpdateValue()
  {
    controller.setInterval(30);
    assertEquals(30, controller.getInterval());
  }

  @Test
  void startPolling_withInvalidInterval_shouldNotStart()
  {
    controller.setInterval(0);
    controller.startPolling(List.of("test"), new HashMap<>(), "json", "out.json", false);

    assertFalse(controller.isPolling());
  }

  @Test
  void startPolling_withInvalidMaxThreads_shouldNotStart()
  {
    controller.setMaxThreads(0);
    controller.setInterval(10);
    controller.startPolling(List.of("test"), new HashMap<>(), "json", "out.json", false);

    assertFalse(controller.isPolling());
  }

  @Test
  void startPolling_withValidSettings_shouldCreatePollingService() throws Exception
  {
    setupClientMock();
    controller.setMaxThreads(2);
    controller.setInterval(1);
    controller.startPolling(List.of("test"), new HashMap<>(), "json", "out.json", false);

    assertTrue(controller.isPolling());
    Thread.sleep(1500);
    controller.stopPolling();
    assertFalse(controller.isPolling());

    verifyFileServiceCalled();
  }

  @Test
  void stopPolling_whenNotPolling_shouldNotThrow()
  {
    assertDoesNotThrow(() -> controller.stopPolling());
  }

  @Test
  void startPolling_withAppendFalse_shouldDeleteExistingFile(@TempDir Path tempDir) throws Exception
  {
    Path file = tempDir.resolve("out.json");
    Files.writeString(file, "dummy");
    when(apiService.getClient("test")).thenReturn(mockClient);
    controller.setMaxThreads(1);
    controller.setInterval(1);
    controller.startPolling(List.of("test"), new HashMap<>(), "json", file.toString(), false);

    assertFalse(Files.exists(file));
    controller.stopPolling();
  }

  @Test
  void startPolling_withAppendTrue_shouldNotDeleteFile(@TempDir Path tempDir) throws Exception
  {
    Path file = tempDir.resolve("out.json");
    Files.writeString(file, "dummy");
    when(apiService.getClient("test")).thenReturn(mockClient);
    controller.setMaxThreads(1);
    controller.setInterval(1);
    controller.startPolling(List.of("test"), new HashMap<>(), "json", file.toString(), true);

    assertTrue(Files.exists(file));
    controller.stopPolling();
  }

  @Test
  void startPolling_whenAlreadyPolling_shouldStopPreviousFirst() throws Exception
  {
    setupClientMock();
    controller.setMaxThreads(1);
    controller.setInterval(1);
    controller.startPolling(List.of("test"), new HashMap<>(), "json", "out.json", false);
    assertTrue(controller.isPolling());

    controller.startPolling(List.of("test"), new HashMap<>(), "json", "out2.json", false);

    assertTrue(controller.isPolling());
    controller.stopPolling();
  }

  @Test
  void startPolling_withIOExceptionOnDelete_shouldPrintErrorAndContinue(@TempDir Path tempDir) throws Exception
  {
    Path dir = Files.createTempDirectory(tempDir, "non_empty_dir");
    Files.writeString(dir.resolve("file.txt"), "content");

    setupClientMock();
    controller.setMaxThreads(1);
    controller.setInterval(1);

    assertDoesNotThrow(() -> controller.startPolling(List.of("test"), new HashMap<>(), "json", dir.toString(), false));

    controller.stopPolling();
  }

  @Test
  void startPolling_withAllNullClients_shouldNotStart()
  {
    when(apiService.getClient("api1")).thenReturn(null);
    when(apiService.getClient("api2")).thenReturn(null);

    controller.setMaxThreads(2);
    controller.setInterval(1);
    controller.startPolling(List.of("api1", "api2"), new HashMap<>(), "json", "out.json", false);

    assertFalse(controller.isPolling());
  }

  @Test
  void startPolling_withNegativeMaxThreads_shouldNotStart()
  {
    controller.setMaxThreads(-5);
    controller.setInterval(10);
    controller.startPolling(List.of("test"), new HashMap<>(), "json", "out.json", false);

    assertFalse(controller.isPolling());
  }

  @Test
  void startPolling_withNegativeInterval_shouldNotStart()
  {
    controller.setInterval(-10);
    controller.startPolling(List.of("test"), new HashMap<>(), "json", "out.json", false);

    assertFalse(controller.isPolling());
  }

  private void setupClientMock() throws Exception
  {
    lenient().when(mockClient.getSourceName()).thenReturn("test");
    lenient().when(apiService.getClient("test")).thenReturn(mockClient);
    ApiResponse mockResponse = new ApiResponse("test", Instant.now(),
      new JsonUtil().getMapper().createObjectNode());
    lenient().when(mockClient.fetchData(any())).thenReturn(mockResponse);
  }

  private void verifyFileServiceCalled() throws FileProcessingException
  {
    verify(fileService, atLeastOnce()).saveRecordsAsJson(anyList(), any(), eq(true));
  }
}
