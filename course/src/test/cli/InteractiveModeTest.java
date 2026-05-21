package cli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.ApiService;
import service.FileService;
import service.InteractiveService;
import service.PollingController;
import util.JsonUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InteractiveModeTest
{
  @Mock
  private ApiService apiService;

  @Mock
  private FileService fileService;

  @Mock
  private InteractiveService interactiveService;

  @Mock
  private PollingController pollingController;

  private JsonUtil jsonUtil;
  private final InputStream originalSystemIn = System.in;

  @BeforeEach
  void setUp()
  {
    jsonUtil = new JsonUtil();
    lenient().when(apiService.getAvailableApiNames()).thenReturn(List.of("chucknorris", "randomuser", "zippopotam"));
    lenient().when(interactiveService.getPollingController()).thenReturn(pollingController);
  }

  @AfterEach
  void restoreSystemIn()
  {
    System.setIn(originalSystemIn);
  }

  @Test
  void constructor_shouldInitializeFields()
  {
    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    assertNotNull(mode);
  }

  @Test
  void interactiveMode_shouldExitOnChoice6() throws Exception
  {
    String input = "6\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));
    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    Thread thread = new Thread(mode::start);
    thread.start();
    Thread.sleep(500);
    if (thread.isAlive())
    {
      thread.interrupt();
    }
    assertFalse(thread.isAlive());
  }

  @Test
  void configurePolling_shouldSetMaxThreadsAndInterval() throws Exception
  {
    doNothing().when(pollingController).setMaxThreads(anyInt());
    doNothing().when(pollingController).setInterval(anyLong());

    String input = "3\n10\n60\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeAndInterrupt(mode);

    verify(pollingController).setMaxThreads(10);
    verify(pollingController).setInterval(60L);
  }

  @Test
  void startPolling_shouldStartWithAllApis() throws Exception
  {
    lenient().when(pollingController.isPolling()).thenReturn(false);
    lenient().when(pollingController.getInterval()).thenReturn(30L);
    doNothing().when(pollingController).startPolling(anyList(), anyMap(), anyString(), anyString(), anyBoolean());

    String input = "4\nall\n\n\n\njson\noutput.json\ny\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeAndInterrupt(mode);

    verify(pollingController).startPolling(anyList(), anyMap(), anyString(), anyString(), anyBoolean());
  }

  @Test
  void startPolling_shouldNotStartIfAlreadyPolling() throws Exception
  {
    when(pollingController.isPolling()).thenReturn(true);

    String input = "4\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeAndInterrupt(mode);

    verify(pollingController, never()).startPolling(anyList(), anyMap(), anyString(), anyString(), anyBoolean());
  }

  @Test
  void startPolling_shouldNotStartIfIntervalNotSet() throws Exception
  {
    when(pollingController.isPolling()).thenReturn(false);
    when(pollingController.getInterval()).thenReturn(0L);

    String input = "4\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeAndInterrupt(mode);

    verify(pollingController, never()).startPolling(anyList(), anyMap(), anyString(), anyString(), anyBoolean());
  }

  @Test
  void stopPolling_shouldStopPolling() throws Exception
  {
    doNothing().when(pollingController).stopPolling();

    String input = "5\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeAndInterrupt(mode);

    verify(pollingController).stopPolling();
  }

  @Test
  void configurePolling_shouldHandleInvalidMaxThreads() throws Exception
  {
    doNothing().when(pollingController).setMaxThreads(anyInt());
    doNothing().when(pollingController).setInterval(anyLong());

    String input = "3\n-5\n0\n8\n30\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeAndInterrupt(mode);

    verify(pollingController).setMaxThreads(8);
  }

  @Test
  void configurePolling_shouldHandleInvalidInterval() throws Exception
  {
    doNothing().when(pollingController).setMaxThreads(anyInt());
    doNothing().when(pollingController).setInterval(anyLong());

    String input = "3\n5\n-10\n60\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeAndInterrupt(mode);

    verify(pollingController).setInterval(60L);
  }

  @Test
  void fetchAndSave_shouldHandleUnknownApi() throws Exception
  {
    String input = "1\nunknown_api\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));
    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    Thread thread = new Thread(() ->
    {
      try
      {
        mode.start();
      }
      catch (Exception e)
      {
      }
    });
    thread.start();
    Thread.sleep(1000);
    if (thread.isAlive())
    {
      thread.interrupt();
    }
  }

  @Test
  void displayFileContent_shouldHandleInvalidFormat() throws Exception
  {
    String input = "2\nfile.json\ninvalid\njson\nall\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));
    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    Thread thread = new Thread(() ->
    {
      try
      {
        mode.start();
      }
      catch (Exception e)
      {
      }
    });
    thread.start();
    Thread.sleep(1000);
    if (thread.isAlive())
    {
      thread.interrupt();
    }
  }

  private void executeAndInterrupt(InteractiveMode mode)
  {
    Thread thread = new Thread(() ->
    {
      try
      {
        mode.start();
      }
      catch (Exception e)
      {
      }
    });
    thread.start();
    try
    {
      Thread.sleep(1000);
    }
    catch (InterruptedException e)
    {
      Thread.currentThread().interrupt();
    }
    if (thread.isAlive())
    {
      thread.interrupt();
    }
  }
}
