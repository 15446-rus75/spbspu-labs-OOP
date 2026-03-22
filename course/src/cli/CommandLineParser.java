package cli;

import org.apache.commons.cli.*;

public class CommandLineParser
{
  private final Options options;

  public CommandLineParser()
  {
    options = new Options();
    options.addOption(Option.builder("m")
                .longOpt("mode")
                .hasArg()
                .desc("Режим: auto или interactive")
                .build());
    options.addOption(Option.builder("a")
                .longOpt("apis")
                .hasArg()
                .desc("Список API через запятую (для auto режима)")
                .build());
    options.addOption(Option.builder("f")
                .longOpt("format")
                .hasArg()
                .desc("Формат файла: json или csv")
                .build());
    options.addOption(Option.builder("o")
                .longOpt("output")
                .hasArg()
                .desc("Выходной файл")
                .build());
  }

  public CommandLine parse(String[] args)
  {
    org.apache.commons.cli.CommandLineParser parser = new DefaultParser();
    try
    {
      return parser.parse(options, args);
    }
    catch (ParseException e)
    {
      System.err.println("Ошибка парсинга аргументов: " + e.getMessage());
      printHelp();
      return null;
    }
  }

  public void printHelp()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("java -jar aggregator.jar [options]", options);
  }
}
