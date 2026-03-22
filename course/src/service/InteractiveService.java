package service;

import exception.FileProcessingException;
import model.AggregatedRecord;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    List<String> apiNames = Arrays.stream(apisParam.split(","))
                .map(String::trim)
                .filter(name -> apiService.getAvailableApiNames().contains(name))
                .collect(Collectors.toList());
    if (apiNames.isEmpty())
    {
      System.out.println("Нет доступных API из списка: " + apisParam);
      return;
    }

    Map<String, Map<String, String>> params = new HashMap<>();
    List<AggregatedRecord> records = apiService.fetchDataFromApis(apiNames, params);
    try
    {
      fileService.saveRecords(records, outputFile, format, false);
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
    fileService.saveRecords(records, filePath, format, append);
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
