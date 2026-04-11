package service;

import api.ApiClient;

import java.util.*;
import java.util.concurrent.*;

public class PollingService
{
  private final ScheduledExecutorService scheduler;
  private final Map< ApiClient, ScheduledFuture< ? > > futures = new ConcurrentHashMap<>();
  private final ApiService apiService;
  private final FileService fileService;

  public PollingService(int maxThreads, ApiService apiService, FileService fileService)
  {
    this.scheduler = Executors.newScheduledThreadPool(maxThreads);
    this.apiService = apiService;
    this.fileService = fileService;
  }

  public void startPolling(Map< String, ApiClient > apiClients, Map< String, Map< String, String > > params,
                           String format, String outputPath, boolean append, long intervalSeconds)
  {
    for (Map.Entry< String, ApiClient > entry : apiClients.entrySet())
    {
      String apiName = entry.getKey();
      ApiClient client = entry.getValue();
      Map< String, String > queryParams = params.getOrDefault(apiName, new HashMap<>());

      PollingTask task = new PollingTask(client, queryParams, format, outputPath, append, fileService, apiService);
      ScheduledFuture< ? > future = scheduler.scheduleWithFixedDelay(task, 0, intervalSeconds, TimeUnit.SECONDS);
      futures.put(client, future);
    }
  }

  public void stopPolling()
  {
    for (ScheduledFuture< ? > future : futures.values())
    {
      future.cancel(true);
    }
    futures.clear();
    scheduler.shutdown();
    try
    {
      if (!scheduler.awaitTermination(10, TimeUnit.SECONDS))
      {
        scheduler.shutdownNow();
      }
    }
    catch (InterruptedException e)
    {
      scheduler.shutdownNow();
    }
  }

  public boolean isPolling()
  {
    return !futures.isEmpty();
  }
}
