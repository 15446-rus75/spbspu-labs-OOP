package service;

import api.ApiClient;
import exception.ApiException;
import model.AggregatedRecord;
import model.ApiResponse;
import util.JsonUtil;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;

public class PollingTask implements Runnable
{
  private final ApiClient client;
  private final Map< String, String > params;
  private final String format;
  private final String outputPath;
  private final boolean append;
  private final FileService fileService;
  private final ApiService apiService;

  public PollingTask(ApiClient client, Map< String, String > params, String format,
                     String outputPath, boolean append, FileService fileService, ApiService apiService)
  {
    this.client = client;
    this.params = params;
    this.format = format;
    this.outputPath = outputPath;
    this.append = append;
    this.fileService = fileService;
    this.apiService = apiService;
  }

  @Override
  public void run()
  {
    try
    {
      ApiResponse response = client.fetchData(params);
      AggregatedRecord record = new AggregatedRecord(client.getSourceName(), response.getTimestamp(), response.getData());

      if ("json".equalsIgnoreCase(format))
      {
        fileService.saveRecordsAsJson(Collections.singletonList(record), Paths.get(outputPath), append);
      }
      else if ("csv".equalsIgnoreCase(format))
      {
        Map< String, Object > flat = client.flattenResponse(response);
        flat.put("id", record.getId());
        flat.put("source", record.getSource());
        flat.put("timestamp", record.getTimestamp().toString());
        fileService.saveRecordsAsCsv(Collections.singletonList(flat), Paths.get(outputPath), append);
      }
      else
      {
        System.err.println("Неподдерживаемый формат: " + format);
      }
    }
    catch (ApiException e)
    {
      System.err.println("Ошибка при опросе " + client.getSourceName() + ": " + e.getMessage());
    }
    catch (Exception e)
    {
      System.err.println("Непредвиденная ошибка при опросе " + client.getSourceName() + ": " + e.getMessage());
    }
  }
}
