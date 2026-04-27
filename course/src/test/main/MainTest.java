package main;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
}
