package cli;

import org.apache.commons.cli.CommandLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandLineParserTest
{

  private CommandLineParser parser;

  @BeforeEach
  void setUp()
  {
    parser = new CommandLineParser();
  }

  @Test
  void parse_autoModeWithAllOptions_shouldReturnCorrectValues()
  {
    String[] args = {"--mode=auto", "--apis=chucknorris,randomuser", "--format=json", "--output=out.json", "--max-threads=8", "--interval=60"};
    CommandLine cmd = parser.parse(args);
    assertNotNull(cmd);
    assertEquals("auto", cmd.getOptionValue("mode"));
    assertEquals("chucknorris,randomuser", cmd.getOptionValue("apis"));
    assertEquals("json", cmd.getOptionValue("format"));
    assertEquals("out.json", cmd.getOptionValue("output"));
    assertEquals("8", cmd.getOptionValue("max-threads"));
    assertEquals("60", cmd.getOptionValue("interval"));
  }

  @Test
  void parse_missingOptionalOptions_shouldReturnNullForThem()
  {
    String[] args = {"--mode=auto", "--apis=test", "--format=csv", "--output=out.csv"};
    CommandLine cmd = parser.parse(args);
    assertNotNull(cmd);
    assertNull(cmd.getOptionValue("max-threads"));
    assertNull(cmd.getOptionValue("interval"));
  }

  @Test
  void parse_interactiveMode_shouldNotRequireOtherOptions()
  {
    String[] args = {"--mode=interactive"};
    CommandLine cmd = parser.parse(args);
    assertNotNull(cmd);
    assertEquals("interactive", cmd.getOptionValue("mode"));
  }

  @Test
  void parse_invalidArgs_shouldReturnNullAndPrintHelp()
  {
    String[] args = {"--unknown"};
    CommandLine cmd = parser.parse(args);
    assertNull(cmd);
  }

  @Test
  void parse_noArgs_shouldReturnEmptyCommandLine()
  {
    String[] args = {};
    CommandLine cmd = parser.parse(args);
    assertNotNull(cmd);
    assertFalse(cmd.hasOption("mode"));
  }
}
