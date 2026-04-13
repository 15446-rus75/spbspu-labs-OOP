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
import static org.mockito.Mockito.*;

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
    when(apiService.getAvailableApiNames()).thenReturn(List.of("chucknorris", "randomuser"));
  }

  @AfterEach
  void restoreSystemIn()
  {
    System.setIn(originalSystemIn);
  }

  @Test
  void constructor_shouldInitializeFields()
  {
    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil);
    assertNotNull(mode);
  }

  @Test
  void interactiveMode_shouldExitOnChoice6()
  {
    String input = "6\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));
    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil);
    Thread thread = new Thread(mode::start);
    thread.start();
    try
    {
      Thread.sleep(500);
      if (thread.isAlive())
      {
        thread.interrupt();
      }
    }
    catch (InterruptedException ignored)
    {
    }
    assertFalse(thread.isAlive());
  }
}
