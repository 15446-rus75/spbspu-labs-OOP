package service;

import api.ApiClient;
import exception.FileProcessingException;
import model.AggregatedRecord;
import model.ApiResponse;
import util.JsonUtil;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class InteractiveService
{
  private final ApiService apiService;
  private final FileService fileService;
  private final DataPrinter printer = new DataPrinter();
  private final JsonUtil jsonUtil = new JsonUtil();

  public InteractiveService(ApiService apiService, FileService fileService)
  {
    this.apiService = apiService;
    this.fileService = fileService;
  }

  public void runAutoMode(String apisParam, String format, String outputFile)
  {
    List<String> apiNames;
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
      if ("json".equalsIgnoreCase(format))
      {
        List<AggregatedRecord> records = fileService.readRecords(filePath, format);
        if (sourceFilter == null)
          printer.printAll(records);
        else
          printer.printBySource(records, sourceFilter);
      }
      else if ("csv".equalsIgnoreCase(format))
      {
        var path = Paths.get(filePath);
        List<String[]> lines = fileService.readRawCsv(path);
        if (lines.isEmpty())
        {
          System.out.println("Файл пуст.");
          return;
        }

        String[] header = lines.get(0);
        int dataIdx = indexOf(header, "data");
        if (dataIdx != -1)
        {
          List<AggregatedRecord> records = new ArrayList<>();
          for (int i = 1; i < lines.size(); i++)
          {
            String[] row = lines.get(i);
            String id = row[indexOf(header, "id")];
            String source = row[indexOf(header, "source")];
            Instant timestamp = Instant.parse(row[indexOf(header, "timestamp")]);
            String dataJson = row[dataIdx];
            var data = jsonUtil.parse(dataJson);
            records.add(new AggregatedRecord(id, source, timestamp, data));
          }
          if (sourceFilter == null)
            printer.printAll(records);
          else
            printer.printBySource(records, sourceFilter);
        }
        else
        {
          List<AggregatedRecord> records = new ArrayList<>();
          for (int i = 1; i < lines.size(); i++)
          {
            String[] row = lines.get(i);
            Map<String, Object> flat = new HashMap<>();
            for (int j = 0; j < header.length; j++)
            {
              flat.put(header[j], row[j]);
            }
            String source = (String) flat.get("source");
            String id = (String) flat.get("id");
            Instant timestamp = Instant.parse((String) flat.get("timestamp"));
            ApiClient client = apiService.getClient(source);
            if (client == null) continue;
            var data = client.unflatten(flat);
            records.add(new AggregatedRecord(id, source, timestamp, data));
          }
          if (sourceFilter == null)
            printer.printAll(records);
          else
            printer.printBySource(records, sourceFilter);
        }
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

  private int indexOf(String[] arr, String target)
  {
    for (int i = 0; i < arr.length; i++)
    {
      if (arr[i].equalsIgnoreCase(target)) return i;
    }
    return -1;
  }
}
