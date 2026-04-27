package service;

import api.ApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PollingServiceTest
{

  @Mock
  private ApiService apiService;
  @Mock
  private FileService fileService;
  @Mock
  private ApiClient mockClient;

  private PollingService pollingService;

  @BeforeEach
  void setUp()
  {
    pollingService = new PollingService(2, apiService, fileService);
  }

  @Test
  void isPolling_shouldReturnFalseWhenNoTasks()
  {
    assertFalse(pollingService.isPolling());
  }

  @Test
  void stopPolling_whenAlreadyStopped_shouldNotThrow()
  {
    assertDoesNotThrow(() -> pollingService.stopPolling());
  }

  @Test
  void startPolling_shouldScheduleTasks() throws InterruptedException
  {
    Map<String, ApiClient> clients = Map.of("test", mockClient);
    Map<String, Map<String, String>> params = new HashMap<>();

    pollingService.startPolling(clients, params, "json", "out.json", true, 1);

    assertTrue(pollingService.isPolling());
    pollingService.stopPolling();
    assertFalse(pollingService.isPolling());
  }
}
