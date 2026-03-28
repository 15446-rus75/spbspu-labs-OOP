package service;

import api.ApiClient;

import java.util.*;

public class PollingController
{
  private final ApiService apiService;
  private final FileService fileService;
  private PollingService pollingService;
  private int maxThreads = 5;
  private long intervalSeconds = 0;

  public PollingController(ApiService apiService, FileService fileService)
  {
    this.apiService = apiService;
    this.fileService = fileService;
  }

  public synchronized void setMaxThreads(int maxThreads)
  {
    this.maxThreads = maxThreads;
  }

  public synchronized void setInterval(long intervalSeconds)
  {
    this.intervalSeconds = intervalSeconds;
  }

  public synchronized int getMaxThreads()
  {
    return maxThreads;
  }

  public synchronized long getInterval()
  {
    return intervalSeconds;
  }

  public synchronized void startPolling(List< String > apiNames, Map< String, Map< String, String > > params,
                                         String format, String outputPath, boolean append)
  {
    if (pollingService != null && pollingService.isPolling())
    {
      stopPolling();
    }
    if (intervalSeconds <= 0)
    {
      System.err.println("Интервал должен быть положительным для периодического опроса.");
      return;
    }
    if (maxThreads <= 0)
    {
      System.err.println("Максимальное количество задач должно быть положительным.");
      return;
    }
    Map< String, ApiClient > apiClients = new HashMap<>();
    for (String name : apiNames)
    {
      ApiClient client = apiService.getClient(name);
      if (client != null)
      {
        apiClients.put(name, client);
      }
    }
    if (apiClients.isEmpty())
    {
      System.err.println("Нет доступных API для опроса.");
      return;
    }
    pollingService = new PollingService(maxThreads, apiService, fileService);
    pollingService.startPolling(apiClients, params, format, outputPath, append, intervalSeconds);
    System.out.println("Опрос запущен с интервалом " + intervalSeconds + " сек и максимальным количеством одновременных задач " + maxThreads);
  }

  public synchronized void stopPolling()
  {
    if (pollingService != null && pollingService.isPolling())
    {
      pollingService.stopPolling();
      pollingService = null;
      System.out.println("Опрос остановлен.");
    }
    else
    {
      System.out.println("Опрос не был запущен.");
    }
  }

  public synchronized boolean isPolling()
  {
    return pollingService != null && pollingService.isPolling();
  }
}
