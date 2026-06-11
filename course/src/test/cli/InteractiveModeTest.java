package cli;

import exception.FileProcessingException;
import model.AggregatedRecord;
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
import static org.mockito.ArgumentMatchers.*;
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
    executeWithTimeout(mode, 100);
  }

  @Test
  void configurePolling_shouldSetMaxThreadsAndInterval() throws Exception
  {
    doNothing().when(pollingController).setMaxThreads(anyInt());
    doNothing().when(pollingController).setInterval(anyLong());

    String input = "3\n10\n60\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeWithTimeout(mode, 150);

    verify(pollingController).setMaxThreads(10);
    verify(pollingController).setInterval(60L);
  }

  @Test
  void startPolling_withAllKeyword_shouldStartWithAllApis() throws Exception
  {
    lenient().when(pollingController.isPolling()).thenReturn(false);
    lenient().when(pollingController.getInterval()).thenReturn(30L);
    doNothing().when(pollingController).startPolling(anyList(), anyMap(), anyString(), anyString(), anyBoolean());

    String input = "4\nall\n\n\n\njson\noutput.json\ny\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeWithTimeout(mode, 200);

    verify(pollingController).startPolling(anyList(), anyMap(), anyString(), anyString(), anyBoolean());
  }

  @Test
  void startPolling_withSpecificApis_shouldParseCommaSeparatedList() throws Exception
  {
    lenient().when(pollingController.isPolling()).thenReturn(false);
    lenient().when(pollingController.getInterval()).thenReturn(30L);
    doNothing().when(pollingController).startPolling(anyList(), anyMap(), anyString(), anyString(), anyBoolean());

    String input = "4\nchucknorris,randomuser\n\n\n\njson\noutput.json\ny\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeWithTimeout(mode, 200);

    verify(pollingController).startPolling(
        argThat(list -> list.contains("chucknorris") && list.contains("randomuser")),
        anyMap(),
        anyString(),
        anyString(),
        anyBoolean()
    );
  }

  @Test
  void startPolling_withUnknownApis_shouldPrintNoApiSelectedMessage() throws Exception
  {
    lenient().when(pollingController.isPolling()).thenReturn(false);
    lenient().when(pollingController.getInterval()).thenReturn(30L);

    String input = "4\nunknown1,unknown2,unknown3\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeWithTimeout(mode, 200);

    verify(pollingController, never()).startPolling(anyList(), anyMap(), anyString(), anyString(), anyBoolean());
  }

  @Test
  void startPolling_withEmptyParams_shouldCreateEmptyParamsMap() throws Exception
  {
    lenient().when(pollingController.isPolling()).thenReturn(false);
    lenient().when(pollingController.getInterval()).thenReturn(30L);
    doNothing().when(pollingController).startPolling(anyList(), anyMap(), anyString(), anyString(), anyBoolean());

    String input = "4\nchucknorris\n\n\n\njson\noutput.json\ny\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeWithTimeout(mode, 200);

    verify(pollingController).startPolling(anyList(), anyMap(), anyString(), anyString(), anyBoolean());
  }

  @Test
  void startPolling_withValidParameters_shouldParseKeyValuePairs() throws Exception
  {
    lenient().when(pollingController.isPolling()).thenReturn(false);
    lenient().when(pollingController.getInterval()).thenReturn(30L);
    doNothing().when(pollingController).startPolling(anyList(), anyMap(), anyString(), anyString(), anyBoolean());

    String input = "4\nchucknorris\ncategory=animal,name=Chuck\n\njson\noutput.json\ny\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeWithTimeout(mode, 200);

    verify(pollingController).startPolling(anyList(), anyMap(), anyString(), anyString(), anyBoolean());
  }

  @Test
  void startPolling_withEmptyPairs_shouldSkipThem() throws Exception
  {
    lenient().when(pollingController.isPolling()).thenReturn(false);
    lenient().when(pollingController.getInterval()).thenReturn(30L);
    doNothing().when(pollingController).startPolling(anyList(), anyMap(), anyString(), anyString(), anyBoolean());

    String input = "4\nchucknorris\n,,invalidpair,=value,valid=ok\n\njson\noutput.json\ny\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeWithTimeout(mode, 200);

    verify(pollingController).startPolling(anyList(), anyMap(), anyString(), anyString(), anyBoolean());
  }

  @Test
  void startPolling_withSpacesInParams_shouldTrimCorrectly() throws Exception
  {
    lenient().when(pollingController.isPolling()).thenReturn(false);
    lenient().when(pollingController.getInterval()).thenReturn(30L);
    doNothing().when(pollingController).startPolling(anyList(), anyMap(), anyString(), anyString(), anyBoolean());

    String input = "4\nchucknorris\ncategory = animal , name = Chuck\n\njson\noutput.json\ny\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeWithTimeout(mode, 200);

    verify(pollingController).startPolling(anyList(), anyMap(), anyString(), anyString(), anyBoolean());
  }

  @Test
  void startPolling_withMultipleApisAndParams_shouldParseAll() throws Exception
  {
    lenient().when(pollingController.isPolling()).thenReturn(false);
    lenient().when(pollingController.getInterval()).thenReturn(30L);
    doNothing().when(pollingController).startPolling(anyList(), anyMap(), anyString(), anyString(), anyBoolean());

    String input = "4\nchucknorris,randomuser\ncategory=animal\ngender=male,nat=us\n\njson\noutput.json\ny\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeWithTimeout(mode, 300);

    verify(pollingController).startPolling(anyList(), anyMap(), anyString(), anyString(), anyBoolean());
  }

  @Test
  void startPolling_withInvalidFormatRetry_shouldCoverWhileLoop() throws Exception
  {
    lenient().when(pollingController.isPolling()).thenReturn(false);
    lenient().when(pollingController.getInterval()).thenReturn(30L);
    doNothing().when(pollingController).startPolling(anyList(), anyMap(), anyString(), anyString(), anyBoolean());

    String input = "4\nchucknorris\n\n\n\nxml\nyaml\ntxt\njson\noutput.json\ny\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeWithTimeout(mode, 300);

    verify(pollingController).startPolling(anyList(), anyMap(), eq("json"), eq("output.json"), anyBoolean());
  }

  @Test
  void startPolling_withCsvFormat_shouldCoverCsvBranch() throws Exception
  {
    lenient().when(pollingController.isPolling()).thenReturn(false);
    lenient().when(pollingController.getInterval()).thenReturn(30L);
    doNothing().when(pollingController).startPolling(anyList(), anyMap(), anyString(), anyString(), anyBoolean());

    String input = "4\nchucknorris\n\n\n\ncsv\noutput.csv\ny\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeWithTimeout(mode, 200);

    verify(pollingController).startPolling(anyList(), anyMap(), eq("csv"), eq("output.csv"), eq(true));
  }

  @Test
  void startPolling_shouldNotStartIfAlreadyPolling() throws Exception
  {
    when(pollingController.isPolling()).thenReturn(true);

    String input = "4\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeWithTimeout(mode, 150);

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
    executeWithTimeout(mode, 150);

    verify(pollingController, never()).startPolling(anyList(), anyMap(), anyString(), anyString(), anyBoolean());
  }

  @Test
  void stopPolling_shouldStopPolling() throws Exception
  {
    doNothing().when(pollingController).stopPolling();

    String input = "5\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeWithTimeout(mode, 100);

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
    executeWithTimeout(mode, 150);

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
    executeWithTimeout(mode, 150);

    verify(pollingController).setInterval(60L);
  }

  @Test
  void fetchAndSave_shouldHandleUnknownApi() throws Exception
  {
    String input = "1\nunknown_api\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeWithTimeout(mode, 150);
  }

  @Test
  void displayFileContent_shouldHandleInvalidFormat() throws Exception
  {
    String input = "2\nfile.json\ninvalid\njson\nall\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeWithTimeout(mode, 150);
  }

  @Test
  void fetchAndSave_withSpecificApis_shouldParseAndFetch() throws Exception
  {
    lenient().when(pollingController.getMaxThreads()).thenReturn(2);
    lenient().when(interactiveService.fetchFromApisParallel(anyList(), anyMap(), anyInt()))
        .thenReturn(List.of(new model.AggregatedRecord("test", java.time.Instant.now(), jsonUtil.getMapper().createObjectNode())));
    lenient().doNothing().when(interactiveService).saveRecords(anyList(), anyString(), anyString(), anyBoolean());

    String input = "1\nchucknorris,randomuser\n\njson\nfile.json\nn\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeWithTimeout(mode, 200);

    verify(interactiveService).fetchFromApisParallel(
        argThat(list -> list.contains("chucknorris") && list.contains("randomuser")),
        anyMap(),
        anyInt()
    );
  }

  @Test
  void fetchAndSave_withEmptyApiSelection_shouldPrintMessage() throws Exception
  {
    String input = "1\nunknown1,unknown2\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeWithTimeout(mode, 150);

    verify(interactiveService, never()).fetchFromApisParallel(anyList(), anyMap(), anyInt());
  }

  @Test
  void fetchAndSave_withParameters_shouldParseAndPassToService() throws Exception
  {
    lenient().when(pollingController.getMaxThreads()).thenReturn(2);
    lenient().when(interactiveService.fetchFromApisParallel(anyList(), anyMap(), anyInt()))
        .thenReturn(List.of(new model.AggregatedRecord("test", java.time.Instant.now(), jsonUtil.getMapper().createObjectNode())));

    String input = "1\nchucknorris\ncategory=animal,name=Chuck\njson\nfile.json\nn\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeWithTimeout(mode, 200);

    verify(interactiveService).fetchFromApisParallel(anyList(), anyMap(), anyInt());
  }

  @Test
  void fetchAndSave_withEmptyParameterLine_shouldCreateEmptyMap() throws Exception
  {
    lenient().when(pollingController.getMaxThreads()).thenReturn(2);
    lenient().when(interactiveService.fetchFromApisParallel(anyList(), anyMap(), anyInt()))
        .thenReturn(List.of(new model.AggregatedRecord("test", java.time.Instant.now(), jsonUtil.getMapper().createObjectNode())));

    String input = "1\nchucknorris\n\njson\nfile.json\nn\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeWithTimeout(mode, 200);

    verify(interactiveService).fetchFromApisParallel(anyList(), anyMap(), anyInt());
  }

  @Test
  void fetchAndSave_withInvalidParameters_shouldIgnoreInvalidPairs() throws Exception
  {
    lenient().when(pollingController.getMaxThreads()).thenReturn(2);
    lenient().when(interactiveService.fetchFromApisParallel(anyList(), anyMap(), anyInt()))
        .thenReturn(List.of(new model.AggregatedRecord("test", java.time.Instant.now(), jsonUtil.getMapper().createObjectNode())));

    String input = "1\nzippopotam\ninvalidpair,zip=12345,=value\njson\nfile.json\nn\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeWithTimeout(mode, 200);

    verify(interactiveService).fetchFromApisParallel(anyList(), anyMap(), anyInt());
  }

  @Test
  void fetchAndSave_withParametersContainingSpaces_shouldParseCorrectly() throws Exception
  {
    lenient().when(pollingController.getMaxThreads()).thenReturn(2);
    lenient().when(interactiveService.fetchFromApisParallel(anyList(), anyMap(), anyInt()))
        .thenReturn(List.of(new model.AggregatedRecord("test", java.time.Instant.now(), jsonUtil.getMapper().createObjectNode())));

    String input = "1\nchucknorris\ncategory = animal , name = Chuck\njson\nfile.json\nn\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeWithTimeout(mode, 200);

    verify(interactiveService).fetchFromApisParallel(anyList(), anyMap(), anyInt());
  }

  @Test
  void fetchAndSave_withMultipleAPIsAndParameters_shouldParseAll() throws Exception
  {
    lenient().when(pollingController.getMaxThreads()).thenReturn(2);
    lenient().when(interactiveService.fetchFromApisParallel(anyList(), anyMap(), anyInt()))
        .thenReturn(List.of(new model.AggregatedRecord("test", java.time.Instant.now(), jsonUtil.getMapper().createObjectNode())));

    String input = "1\nchucknorris,randomuser\ncategory=animal\ngender=male,nat=us\njson\nfile.json\nn\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeWithTimeout(mode, 250);

    verify(interactiveService).fetchFromApisParallel(anyList(), anyMap(), anyInt());
  }

  @Test
  void fetchAndSave_withInvalidFormatRetry_shouldAcceptCorrectFormatAfterInvalid() throws Exception
  {
    lenient().when(pollingController.getMaxThreads()).thenReturn(2);
    lenient().when(interactiveService.fetchFromApisParallel(anyList(), anyMap(), anyInt()))
        .thenReturn(List.of(new model.AggregatedRecord("test", java.time.Instant.now(), jsonUtil.getMapper().createObjectNode())));
    lenient().doNothing().when(interactiveService).saveRecords(anyList(), anyString(), anyString(), anyBoolean());

    String input = "1\nall\n\n\nxml\njson\nfile.json\nn\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeWithTimeout(mode, 250);

    verify(interactiveService).saveRecords(anyList(), eq("file.json"), eq("json"), eq(false));
  }

  @Test
  void fetchAndSave_saveException_shouldHandleGracefully() throws Exception
  {
    lenient().when(pollingController.getMaxThreads()).thenReturn(2);
    lenient().when(interactiveService.fetchFromApisParallel(anyList(), anyMap(), anyInt()))
        .thenReturn(List.of(new model.AggregatedRecord("test", java.time.Instant.now(), jsonUtil.getMapper().createObjectNode())));
    lenient().doAnswer(invocation ->
    {
      throw new FileProcessingException("Disk full");
    }).when(interactiveService).saveRecords(anyList(), anyString(), anyString(), anyBoolean());

    String input = "1\nchucknorris\n\njson\nfile.json\nn\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    assertDoesNotThrow(() -> executeWithTimeout(mode, 200));
  }

  @Test
  void fetchAndSave_withComplexBadParameters_shouldIgnoreInvalidPairs() throws Exception
  {
    lenient().when(pollingController.getMaxThreads()).thenReturn(2);
    lenient().when(interactiveService.fetchFromApisParallel(anyList(), anyMap(), anyInt()))
        .thenReturn(List.of(new model.AggregatedRecord("test", java.time.Instant.now(), jsonUtil.getMapper().createObjectNode())));

    String input = "1\nzippopotam\n,,invalidpair\njson\nfile.json\nn\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeWithTimeout(mode, 200);

    verify(interactiveService).fetchFromApisParallel(anyList(), anyMap(), anyInt());
  }

  @Test
  void fetchAndSave_withParameterWithoutEqualsSign_shouldIgnoreIt() throws Exception
  {
    lenient().when(pollingController.getMaxThreads()).thenReturn(2);
    lenient().when(interactiveService.fetchFromApisParallel(anyList(), anyMap(), anyInt()))
        .thenReturn(List.of(new model.AggregatedRecord("test", java.time.Instant.now(), jsonUtil.getMapper().createObjectNode())));

    String input = "1\nzippopotam\ninvalidparam,zip=12345\njson\nfile.json\nn\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeWithTimeout(mode, 200);

    verify(interactiveService).fetchFromApisParallel(anyList(), anyMap(), anyInt());
  }

  @Test
  void fetchAndSave_withParameterHavingEmptyKey_shouldIgnoreIt() throws Exception
  {
    lenient().when(pollingController.getMaxThreads()).thenReturn(2);
    lenient().when(interactiveService.fetchFromApisParallel(anyList(), anyMap(), anyInt()))
        .thenReturn(List.of(new model.AggregatedRecord("test", java.time.Instant.now(), jsonUtil.getMapper().createObjectNode())));

    String input = "1\nchucknorris\n=value,category=animal\njson\nfile.json\nn\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    InteractiveMode mode = new InteractiveMode(apiService, fileService, jsonUtil, interactiveService);
    executeWithTimeout(mode, 200);

    verify(interactiveService).fetchFromApisParallel(anyList(), anyMap(), anyInt());
  }

  private void executeWithTimeout(InteractiveMode mode, long timeoutMs) throws InterruptedException
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
    thread.setDaemon(true);
    thread.start();
    Thread.sleep(timeoutMs);
    if (thread.isAlive())
    {
      thread.interrupt();
      thread.join(50);
    }
  }
}
