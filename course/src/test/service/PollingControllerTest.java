package service;

import api.ApiClient;
import model.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
  void startPolling_withValidSettings_shouldCreatePollingService() throws Exception
  {
    when(mockClient.getSourceName()).thenReturn("test");
    when(apiService.getClient("test")).thenReturn(mockClient);
    ApiResponse mockResponse = new ApiResponse("test", Instant.now(),
            new util.JsonUtil().getMapper().createObjectNode());
    when(mockClient.fetchData(any())).thenReturn(mockResponse);

    controller.setMaxThreads(2);
    controller.setInterval(1);
    controller.startPolling(List.of("test"), new HashMap<>(), "json", "out.json", false);

    assertTrue(controller.isPolling());
    Thread.sleep(1500);
    controller.stopPolling();
    assertFalse(controller.isPolling());

    verify(fileService, atLeastOnce()).saveRecordsAsJson(anyList(), any(), eq(true));
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
}
