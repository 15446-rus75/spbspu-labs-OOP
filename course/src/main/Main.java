package main;

import cli.CommandLineParser;
import cli.InteractiveMode;
import config.AppConfig;
import service.ApiService;
import service.FileService;
import service.InteractiveService;
import util.HttpClientUtil;
import util.JsonUtil;
import org.apache.commons.cli.CommandLine;

public class Main
{
  public static void main(String[] args)
  {
    HttpClientUtil httpClient = new HttpClientUtil();
    JsonUtil jsonUtil = new JsonUtil();
    AppConfig appConfig = new AppConfig();
    ApiService apiService = new ApiService(appConfig.getApiClients(), httpClient, jsonUtil);
    FileService fileService = new FileService(jsonUtil);

    CommandLineParser parser = new CommandLineParser();
    CommandLine cmd = parser.parse(args);

    if (cmd != null && cmd.hasOption("mode") && cmd.getOptionValue("mode").equalsIgnoreCase("auto"))
    {
      String apis = cmd.getOptionValue("apis");
      String format = cmd.getOptionValue("format");
      String output = cmd.getOptionValue("output");

      if (apis == null || format == null || output == null)
      {
        System.err.println("Для автоматического режима укажите --apis, --format и --output");
        parser.printHelp();
        return;
      }

      InteractiveService interactiveService = new InteractiveService(apiService, fileService, jsonUtil);
      interactiveService.runAutoMode(apis, format, output);
    }
    else
    {
      InteractiveMode interactiveMode = new InteractiveMode(apiService, fileService, jsonUtil);
      interactiveMode.start();
    }
  }
}
