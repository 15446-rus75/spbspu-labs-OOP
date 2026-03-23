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
    if (records.isEmpty()) return;
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

    String[] header;
    if (append && fileExists)
    {
      try (CSVReader reader = new CSVReader(new FileReader(path.toFile())))
      {
        List<String[]> lines = reader.readAll();
        if (!lines.isEmpty())
        {
          header = lines.get(0);
        }
        else
        {
          header = null;
        }
      }
      catch (IOException | CsvException e)
      {
        throw new FileProcessingException("Ошибка чтения заголовка CSV", e);
      }
    }
    else
    {
      Set<String> allKeys = new LinkedHashSet<>();
      allKeys.add("id");
      allKeys.add("source");
      allKeys.add("timestamp");
      for (Map<String, Object> record : flatRecords)
      {
        allKeys.addAll(record.keySet());
      }
      header = allKeys.toArray(new String[0]);
    }

    if (header == null)
    {
      throw new FileProcessingException("Не удалось определить заголовок CSV");
    }

    try (CSVWriter writer = new CSVWriter(new FileWriter(path.toFile(), append)))
    {
      if (!append || !fileExists)
      {
        writer.writeNext(header);
      }

      for (Map<String, Object> record : flatRecords)
      {
        String[] line = new String[header.length];
        for (int i = 0; i < header.length; ++i)
        {
          Object value = record.get(header[i]);
          line[i] = value != null ? value.toString() : "";
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

  public List<String[]> readRawCsv(Path path) throws FileProcessingException
  {
    try (CSVReader reader = new CSVReader(new FileReader(path.toFile())))
    {
      return reader.readAll();
    }
    catch (IOException | CsvException e)
    {
      throw new FileProcessingException("Ошибка чтения CSV", e);
    }
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
    else
    {
      throw new FileProcessingException("Неподдерживаемый формат для чтения: " + format);
    }
  }
}
