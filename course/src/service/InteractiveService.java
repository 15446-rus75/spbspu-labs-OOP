package service;

import api.ApiClient;
import exception.FileProcessingException;
import model.AggregatedRecord;
import model.ApiResponse;

import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class InteractiveService
{
  private final ApiService apiService;
  private final FileService fileService;
  private final DataPrinter printer = new DataPrinter();

  public InteractiveService(ApiService apiService, FileService fileService)
  {
    this.apiService = apiService;
    this.fileService = fileService;
  }

  public void runAutoMode(String apisParam, String format, String outputFile)
  {
    List<String> apiNames;
    if (apisParam.equalsIgnoreCase("all")) {
      apiNames = apiService.getAvailableApiNames();
    } else {
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

    Map<String, Map<String, String>> params = new HashMap<>();
    List<AggregatedRecord> records = apiService.fetchDataFromApis(apiNames, params);
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

  public List<AggregatedRecord> fetchFromApis(List<String> apiNames, Map<String, Map<String, String>> params)
  {
    return apiService.fetchDataFromApis(apiNames, params);
  }

  public void saveRecords(List<AggregatedRecord> records, String filePath, String format, boolean append)
            throws FileProcessingException
  {
    var path = Paths.get(filePath);
    if ("json".equalsIgnoreCase(format))
    {
      fileService.saveRecordsAsJson(records, path, append);
    }
    else if ("csv".equalsIgnoreCase(format))
    {
      List<Map<String, Object>> flatRecords = new ArrayList<>();
      for (AggregatedRecord record : records)
      {
        ApiClient client = apiService.getClient(record.getSource());
        if (client == null) continue;
        ApiResponse apiResponse = new ApiResponse(record.getSource(), record.getTimestamp(), record.getData());
        Map<String, Object> flat = client.flattenResponse(apiResponse);
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
      List<AggregatedRecord> records = fileService.readRecords(filePath, format);
      if (sourceFilter == null)
      {
        printer.printAll(records);
      }
      else
      {
        printer.printBySource(records, sourceFilter);
      }
    }
    catch (FileProcessingException e)
    {
      System.err.println("Ошибка чтения файла: " + e.getMessage());
    }
  }
}
