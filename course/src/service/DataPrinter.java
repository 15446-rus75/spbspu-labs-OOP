package service;

import model.AggregatedRecord;
import java.util.List;
import java.util.stream.Collectors;

public class DataPrinter
{
  public void printAll(List<AggregatedRecord> records)
  {
    if (records.isEmpty())
    {
      System.out.println("Файл пуст.");
      return;
    }
    for (AggregatedRecord rec : records)
    {
      printRecord(rec);
    }
  }

  public void printBySource(List<AggregatedRecord> records, String source)
  {
    List<AggregatedRecord> filtered = records.stream()
                .filter(r -> r.getSource().equalsIgnoreCase(source))
                .collect(Collectors.toList());
    if (filtered.isEmpty())
    {
      System.out.println("Записей для источника " + source + " не найдено.");
    }
    else
    {
      filtered.forEach(this::printRecord);
    }
  }

  private void printRecord(AggregatedRecord rec)
  {
    System.out.println("ID: " + rec.getId());
    System.out.println("Source: " + rec.getSource());
    System.out.println("Timestamp: " + rec.getTimestamp());
    System.out.println("Data: " + rec.getData().toPrettyString());
    System.out.println("------------------------");
  }
}
