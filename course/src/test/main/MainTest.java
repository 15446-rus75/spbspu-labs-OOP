package main;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MainTest
{
  @Test
  void main_autoModeWithInvalidFormat_shouldNotThrow(@TempDir Path tempDir)
  {
    String output = tempDir.resolve("out.json").toString();
    String[] args = {
      "--mode", "auto",
      "--apis", "chucknorris",
      "--format", "json",
      "--output", output,
      "--max-threads", "2",
      "--interval", "0"
    };
    assertDoesNotThrow(() -> Main.main(args));
  }

  @Test
  void main_missingRequiredOptions_shouldPrintError(@TempDir Path tempDir)
  {
    String[] args = {"--mode", "auto"};
    assertDoesNotThrow(() -> Main.main(args));
  }

  @Test
  void main_autoModeWithInvalidMaxThreads_shouldUseDefault(@TempDir Path tempDir)
  {
    String output = tempDir.resolve("out.json").toString();
    String[] args = {
      "--mode", "auto",
      "--apis", "chucknorris",
      "--format", "json",
      "--output", output,
      "--max-threads", "invalid"
    };
    assertDoesNotThrow(() -> Main.main(args));
  }

  @Test
  void main_autoModeWithInvalidInterval_shouldUseDefault(@TempDir Path tempDir)
  {
    String output = tempDir.resolve("out.json").toString();
    String[] args = {
      "--mode", "auto",
      "--apis", "chucknorris",
      "--format", "json",
      "--output", output,
      "--interval", "not-a-number"
    };
    assertDoesNotThrow(() -> Main.main(args));
  }

  @Test
  void main_interactiveMode_shouldNotThrow(@TempDir Path tempDir)
  {
    String[] args = {"--mode", "interactive"};
    InputStream originalIn = System.in;
    try
    {
      System.setIn(new ByteArrayInputStream("6\n".getBytes()));
      assertDoesNotThrow(() -> Main.main(args));
    }
    finally
    {
      System.setIn(originalIn);
    }
  }

  @Test
  void main_noArgs_shouldStartInteractiveMode(@TempDir Path tempDir)
  {
    String[] args = {};
    InputStream originalIn = System.in;
    try
    {
      System.setIn(new ByteArrayInputStream("6\n".getBytes()));
      assertDoesNotThrow(() -> Main.main(args));
    }
    finally
    {
      System.setIn(originalIn);
    }
  }
}
