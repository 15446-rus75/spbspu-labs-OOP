package service;

import exception.FileProcessingException;
import model.AggregatedRecord;
import util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class FileService
{
  private final JsonUtil jsonUtil;

  public FileService(JsonUtil jsonUtil)
  {
    this.jsonUtil = jsonUtil;
  }

  public void saveRecordsAsJson(List<AggregatedRecord> records, Path path, boolean append)
            throws FileProcessingException
  {
    if (records.isEmpty())
    {
      return;
    }
    boolean fileExists = Files.exists(path);
    try
    {
      List<AggregatedRecord> existing = new ArrayList<>();
      if (append && fileExists)
      {
        existing = readRecordsFromJson(path);
      }
      existing.addAll(records);
      jsonUtil.write(path, existing.stream().map(AggregatedRecord::toJson).collect(Collectors.toList()));
    }
    catch (IOException e)
    {
      throw new FileProcessingException("Ошибка записи JSON", e);
    }
  }

  public void saveRecordsAsCsv(List<Map<String, Object>> flatRecords, Path path, boolean append)
            throws FileProcessingException
  {
    if (flatRecords.isEmpty())
    {
      return;
    }
    boolean fileExists = Files.exists(path);
    try (CSVWriter writer = new CSVWriter(new FileWriter(path.toFile(), append)))
    {
      if (!append || !fileExists)
      {
        Set<String> allKeys = new LinkedHashSet<>();
        allKeys.add("id");
        allKeys.add("source");
        allKeys.add("timestamp");
        for (Map<String, Object> record : flatRecords)
        {
          allKeys.addAll(record.keySet());
        }
        String[] header = allKeys.toArray(new String[0]);
        writer.writeNext(header);
      }

      String[] header = null;
      if (append && fileExists)
      {
        try (CSVReader reader = new CSVReader(new FileReader(path.toFile())))
        {
          List<String[]> lines = reader.readAll();
          if (!lines.isEmpty())
          {
            header = lines.get(0);
          }
        }
        catch (IOException | CsvException e)
        {
          throw new FileProcessingException("Ошибка чтения заголовка CSV", e);
        }
      }

      for (Map<String, Object> record : flatRecords)
      {
        String[] line;
        if (header != null)
        {
          line = new String[header.length];
          for (int i = 0; i < header.length; i++)
          {
            Object value = record.get(header[i]);
            line[i] = value != null ? value.toString() : "";
          }
        }
        else
        {
          List<String> keys = new ArrayList<>(record.keySet());
          line = new String[keys.size()];
          for (int i = 0; i < keys.size(); i++)
          {
            Object value = record.get(keys.get(i));
            line[i] = value != null ? value.toString() : "";
          }
        }
        writer.writeNext(line);
      }
    }
    catch (IOException e)
    {
      throw new FileProcessingException("Ошибка записи CSV", e);
    }
  }

  private List<AggregatedRecord> readRecordsFromJson(Path path) throws IOException
  {
    ArrayNode array = jsonUtil.readArray(path);
    List<AggregatedRecord> list = new ArrayList<>();
    for (JsonNode node : array)
    {
      String id = node.path("id").asText();
      String source = node.path("source").asText();
      Instant timestamp = Instant.parse(node.path("timestamp").asText());
      JsonNode data = node.path("data");
      list.add(new AggregatedRecord(id, source, timestamp, data));
    }
    return list;
  }

  public List<AggregatedRecord> readRecords(String filePath, String format) throws FileProcessingException
  {
    Path path = Paths.get(filePath);
    if (!Files.exists(path))
    {
      return Collections.emptyList();
    }
    if ("json".equalsIgnoreCase(format))
    {
      try
      {
        return readRecordsFromJson(path);
      }
      catch (IOException e)
      {
        throw new FileProcessingException("Ошибка чтения JSON", e);
      }
    }
    else if ("csv".equalsIgnoreCase(format))
    {
      return readRecordsFromCsv(path);
    }
    else
    {
      throw new FileProcessingException("Неподдерживаемый формат: " + format);
    }
  }

  private List<AggregatedRecord> readRecordsFromCsv(Path path) throws FileProcessingException
  {
    List<AggregatedRecord> list = new ArrayList<>();
    try (CSVReader reader = new CSVReader(new FileReader(path.toFile())))
    {
      List<String[]> lines = reader.readAll();
      if (lines.isEmpty())
      {
        return list;
      }
      String[] header = lines.get(0);
      int idIdx = indexOf(header, "id");
      int srcIdx = indexOf(header, "source");
      int tsIdx = indexOf(header, "timestamp");
      int dataIdx = indexOf(header, "data");
      if (idIdx == -1 || srcIdx == -1 || tsIdx == -1 || dataIdx == -1)
      {
        throw new FileProcessingException("CSV файл не содержит обязательных колонок");
      }
      for (int i = 1; i < lines.size(); ++i)
      {
        String[] line = lines.get(i);
        String id = line[idIdx];
        String source = line[srcIdx];
        Instant timestamp = Instant.parse(line[tsIdx]);
        JsonNode data = jsonUtil.parse(line[dataIdx]);
        list.add(new AggregatedRecord(id, source, timestamp, data));
      }
    }
    catch (IOException | CsvException e)
    {
      throw new FileProcessingException("Ошибка чтения CSV", e);
    }
    return list;
  }

  private int indexOf(String[] arr, String target)
  {
    for (int i = 0; i < arr.length; ++i)
    {
      if (arr[i].equalsIgnoreCase(target))
      {
        return i;
      }
    }
    return -1;
  }
}
