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

  public void saveRecords(List<AggregatedRecord> records, String filePath, String format, boolean append)
            throws FileProcessingException
  {
    if (records.isEmpty())
    {
      return;
    }
    Path path = Paths.get(filePath);
    boolean fileExists = Files.exists(path);

    if ("json".equalsIgnoreCase(format))
    {
      saveAsJson(records, path, append, fileExists);
    }
    else if ("csv".equalsIgnoreCase(format))
    {
      saveAsCsv(records, path, append, fileExists);
    }
    else
    {
      throw new FileProcessingException("Неподдерживаемый формат: " + format);
    }
  }

  private void saveAsJson(List<AggregatedRecord> records, Path path, boolean append, boolean fileExists)
            throws FileProcessingException
  {
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

  private void saveAsCsv(List<AggregatedRecord> records, Path path, boolean append, boolean fileExists)
            throws FileProcessingException
  {
    try (CSVWriter writer = new CSVWriter(new FileWriter(path.toFile(), append)))
    {
      if (!append || !fileExists)
      {
        writer.writeNext(new String[]{"id", "source", "timestamp", "data"});
      }
      for (AggregatedRecord rec : records)
      {
        String[] line = new String[]{ rec.getId(), rec.getSource(), rec.getTimestamp().toString(),
          rec.getData().toString() };
        writer.writeNext(line);
      }
    }
    catch (IOException e)
    {
      throw new FileProcessingException("Ошибка записи CSV", e);
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
