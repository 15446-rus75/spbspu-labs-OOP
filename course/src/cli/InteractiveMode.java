package cli;

import exception.FileProcessingException;
import model.AggregatedRecord;
import service.ApiService;
import service.FileService;
import service.InteractiveService;
import util.JsonUtil;

import java.util.*;

public class InteractiveMode
{
  private final InteractiveService interactiveService;
  private final ConsoleInputHandler inputHandler;
  private final List< String > availableApis;

  public InteractiveMode(ApiService apiService, FileService fileService, JsonUtil jsonUtil)
  {
    this.interactiveService = new InteractiveService(apiService, fileService, jsonUtil);
    this.inputHandler = new ConsoleInputHandler();
    this.availableApis = apiService.getAvailableApiNames();
  }

  public void start()
  {
    System.out.println("Добро пожаловать в агрегатор данных (интерактивный режим)");
    boolean exit = false;
    while (!exit)
    {
      printMenu();
      int choice = inputHandler.readInt("Выберите действие: ");
      switch (choice)
      {
        case 1 -> fetchAndSave();
        case 2 -> displayFileContent();
        case 3 -> exit = true;
        default -> System.out.println("Неверный ввод, попробуйте снова.");
      }
    }
    inputHandler.close();
  }

  private void printMenu()
  {
    System.out.println("\n--- Меню ---");
    System.out.println("1. Опрос API и сохранение в файл");
    System.out.println("2. Вывести содержимое файла");
    System.out.println("3. Выход");
  }

  private void fetchAndSave()
  {
    System.out.println("Доступные API: " + availableApis);
    String chosen = inputHandler.readString("Введите названия API через запятую (или 'all' для всех): ");
    List< String > apisToFetch;
    if (chosen.equalsIgnoreCase("all"))
    {
      apisToFetch = new ArrayList<>(availableApis);
    }
    else
    {
      apisToFetch = Arrays.stream(chosen.split(","))
                    .map(String::trim)
                    .filter(availableApis::contains)
                    .toList();
      if (apisToFetch.isEmpty())
      {
        System.out.println("Не выбрано ни одного доступного API.");
        return;
      }
    }

    Map< String, Map< String, String > > params = new HashMap<>();
    for (String api : apisToFetch)
    {
      System.out.println("Введите параметры для " + api + " (в формате ключ=значение, пусто если нет):");
      String paramLine = inputHandler.readString("");
      if (!paramLine.isBlank())
      {
        Map< String, String > map = new HashMap<>();
        String[] pairs = paramLine.split(",");
        for (String pair : pairs)
        {
          pair = pair.trim();
          if (pair.isEmpty())
          {
            continue;
          }
          String[] kv = pair.split("=");
          if (kv.length == 2)
          {
            String key = kv[0].trim();
            String value = kv[1].trim();
            if (!key.isEmpty())
            {
              map.put(key, value);
            }
          }
        }
        params.put(api, map);
      }
    }

    List< AggregatedRecord > records = interactiveService.fetchFromApis(apisToFetch, params);
    if (records.isEmpty())
    {
      System.out.println("Нет полученных данных.");
      return;
    }

    String format = inputHandler.readString("Формат файла (json/csv): ");
    while (!format.equalsIgnoreCase("json") && !format.equalsIgnoreCase("csv"))
    {
      format = inputHandler.readString("Неверный формат. Введите json или csv: ");
    }

    String filePath = inputHandler.readString("Путь к файлу: ");
    boolean append = inputHandler.readBoolean("Дозаписать в существующий файл? (y/n): ");

    try
    {
      interactiveService.saveRecords(records, filePath, format, append);
      System.out.println("Данные сохранены.");
    }
    catch (FileProcessingException e)
    {
      System.err.println("Ошибка сохранения: " + e.getMessage());
    }
  }

  private void displayFileContent()
  {
    String filePath = inputHandler.readString("Путь к файлу: ");
    String format = inputHandler.readString("Формат файла (json/csv): ");
    while (!format.equalsIgnoreCase("json") && !format.equalsIgnoreCase("csv"))
    {
      format = inputHandler.readString("Неверный формат. Введите json или csv: ");
    }
    String filter = inputHandler.readString("Вывести всё (введите 'all') или укажите конкретный источник: ");
    String source = filter.equalsIgnoreCase("all") ? null : filter;
    interactiveService.displayRecords(filePath, format, source);
  }
}
