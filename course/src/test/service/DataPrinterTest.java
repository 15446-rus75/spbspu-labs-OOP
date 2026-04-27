package service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import model.AggregatedRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.JsonUtil;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataPrinterTest
{

  private DataPrinter printer;
  private JsonUtil jsonUtil;
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;

  @BeforeEach
  void setUp()
  {
    printer = new DataPrinter();
    jsonUtil = new JsonUtil();
    System.setOut(new PrintStream(outContent));
  }

  @AfterEach
  void restoreStreams()
  {
    System.setOut(originalOut);
  }

  @Test
  void printAll_shouldPrintAllRecords()
  {
    ObjectNode data1 = jsonUtil.getMapper().createObjectNode().put("field", "value1");
    ObjectNode data2 = jsonUtil.getMapper().createObjectNode().put("field", "value2");
    AggregatedRecord rec1 = new AggregatedRecord("src1", Instant.now(), data1);
    AggregatedRecord rec2 = new AggregatedRecord("src2", Instant.now(), data2);

    printer.printAll(List.of(rec1, rec2));

    String output = outContent.toString();
    assertTrue(output.contains("src1"));
    assertTrue(output.contains("src2"));
    assertTrue(output.contains("value1"));
    assertTrue(output.contains("value2"));
  }

  @Test
  void printAll_emptyList_shouldPrintMessage()
  {
    printer.printAll(List.of());
    String output = outContent.toString();
    assertTrue(output.contains("Файл пуст"));
  }

  @Test
  void printBySource_shouldFilterAndPrint()
  {
    ObjectNode data1 = jsonUtil.getMapper().createObjectNode().put("id", 1);
    ObjectNode data2 = jsonUtil.getMapper().createObjectNode().put("id", 2);
    AggregatedRecord rec1 = new AggregatedRecord("srcA", Instant.now(), data1);
    AggregatedRecord rec2 = new AggregatedRecord("srcB", Instant.now(), data2);

    printer.printBySource(List.of(rec1, rec2), "srcA");

    String output = outContent.toString();
    assertTrue(output.contains("srcA"));
    assertFalse(output.contains("srcB"));
  }

  @Test
  void printBySource_noMatchingRecords_shouldPrintMessage()
  {
    AggregatedRecord rec = new AggregatedRecord("srcA", Instant.now(), jsonUtil.getMapper().createObjectNode());
    printer.printBySource(List.of(rec), "srcB");

    String output = outContent.toString();
    assertTrue(output.contains("Записей для источника srcB не найдено"));
  }
}
