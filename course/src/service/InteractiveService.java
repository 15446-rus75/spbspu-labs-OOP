package service;

import api.ApiClient;
import exception.ApiException;
import exception.FileProcessingException;
import model.AggregatedRecord;
import model.ApiResponse;
import util.JsonUtil;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class InteractiveService
{
  private final ApiService apiService;
  private final FileService fileService;
  private final DataPrinter printer;
  private final JsonUtil jsonUtil;
  private final PollingController pollingController;

  public InteractiveService(ApiService apiService, FileService fileService, JsonUtil jsonUtil)
  {
    this.apiService = apiService;
    this.fileService = fileService;
    this.printer = new DataPrinter();
    this.jsonUtil = jsonUtil;
    this.pollingController = new PollingController(apiService, fileService);
  }

  public void runAutoMode(String apisParam, String format, String outputFile, int maxThreads, long interval)
  {
    List< String > apiNames;
    if (apisParam.equalsIgnoreCase("all"))
    {
      apiNames = apiService.getAvailableApiNames();
    }
    else
    {
      apiNames = Arrays.stream(apisParam.split(","))
                .map(String::trim)
                .filter(name -> apiService.getAvailableApiNames().contains(name))
                .collect(Collectors.toList());
    }
    if (apiNames.isEmpty())
    {
      System.out.println("Нет доступных API из списка: " + apisParam);
      return;
    }

    Map< String, Map< String, String > > params = new HashMap<>();
    if (interval > 0)
    {
      pollingController.setMaxThreads(maxThreads);
      pollingController.setInterval(interval);
      pollingController.startPolling(apiNames, params, format, outputFile, true);
      System.out.println("Опрос запущен. Нажмите Enter для остановки...");
      try
      {
        System.in.read();
      }
      catch (Exception e)
      {
      }
      pollingController.stopPolling();
    }
    else
    {
      List< AggregatedRecord > records = fetchFromApisParallel(apiNames, params, maxThreads);
      if (records.isEmpty())
      {
        System.out.println("Нет полученных данных.");
        return;
      }

      try
      {
        saveRecords(records, outputFile, format, false);
        System.out.println("Данные сохранены в " + outputFile);
      }
      catch (FileProcessingException e)
      {
        System.err.println("Ошибка сохранения: " + e.getMessage());
      }
    }
  }

  public List< AggregatedRecord > fetchFromApisParallel(List< String > apiNames, Map< String, Map< String, String > > params, int maxThreads)
  {
    ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
    List< Future< AggregatedRecord > > futures = new ArrayList<>();
    for (String name : apiNames)
    {
      ApiClient client = apiService.getClient(name);
      if (client == null)
      {
        System.err.println("Предупреждение: API '" + name + "' не найден");
        continue;
      }
      Map< String, String > queryParams = params.getOrDefault(name, new HashMap<>());
      futures.add(executor.submit(() -> {
        try
        {
          ApiResponse response = client.fetchData(queryParams);
          return new AggregatedRecord(name, response.getTimestamp(), response.getData());
        }
        catch (ApiException e)
        {
          System.err.println("Ошибка при получении данных от " + name + ": " + e.getMessage());
          return null;
        }
      }));
    }

    List< AggregatedRecord > records = new ArrayList<>();
    for (Future< AggregatedRecord > future : futures)
    {
      try
      {
        AggregatedRecord record = future.get();
        if (record != null)
        {
          records.add(record);
        }
      }
      catch (InterruptedException | ExecutionException e)
      {
        System.err.println("Ошибка при получении результата: " + e.getMessage());
      }
    }
    executor.shutdown();
    try
    {
      if (!executor.awaitTermination(5, TimeUnit.SECONDS))
      {
        executor.shutdownNow();
      }
    }
    catch (InterruptedException e)
    {
      executor.shutdownNow();
    }
    return records;
  }

  public List< AggregatedRecord > fetchFromApis(List< String > apiNames, Map< String, Map< String, String > > params)
  {
    return apiService.fetchDataFromApis(apiNames, params);
  }

  public void saveRecords(List< AggregatedRecord > records, String filePath, String format, boolean append)
            throws FileProcessingException
  {
    var path = Paths.get(filePath);
    if ("json".equalsIgnoreCase(format))
    {
      fileService.saveRecordsAsJson(records, path, append);
    }
    else if ("csv".equalsIgnoreCase(format))
    {
      List< Map< String, Object > > flatRecords = new ArrayList<>();
      for (AggregatedRecord record : records)
      {
        ApiClient client = apiService.getClient(record.getSource());
        if (client == null)
        {
          continue;
        }
        ApiResponse apiResponse = new ApiResponse(record.getSource(), record.getTimestamp(), record.getData());
        Map< String, Object > flat = client.flattenResponse(apiResponse);
        flat.put("id", record.getId());
        flat.put("source", record.getSource());
        flat.put("timestamp", record.getTimestamp().toString());
        flatRecords.add(flat);
      }
      fileService.saveRecordsAsCsv(flatRecords, path, append);
    }
    else
    {
      throw new FileProcessingException("Неподдерживаемый формат: " + format);
    }
  }

  public void displayRecords(String filePath, String format, String sourceFilter)
  {
    try
    {
      if ("json".equalsIgnoreCase(format))
      {
        List< AggregatedRecord > records = fileService.readRecords(filePath, format);
        printRecords(records, sourceFilter);
      }
      else if ("csv".equalsIgnoreCase(format))
      {
        var path = Paths.get(filePath);
        List< AggregatedRecord > records = fileService.readCsvRecords(path, apiService);
        printRecords(records, sourceFilter);
      }
      else
      {
        System.err.println("Неподдерживаемый формат: " + format);
      }
    }
    catch (Exception e)
    {
      System.err.println("Ошибка чтения файла: " + e.getMessage());
    }
  }

  private void printRecords(List< AggregatedRecord > records, String sourceFilter)
  {
    if (sourceFilter == null)
    {
      printer.printAll(records);
    }
    else
    {
      printer.printBySource(records, sourceFilter);
    }
  }

  public PollingController getPollingController()
  {
    return pollingController;
  }
}
