package cli;

import exception.FileProcessingException;
import model.AggregatedRecord;
import service.ApiService;
import service.FileService;
import service.InteractiveService;
import util.JsonUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InteractiveMode
{
  private final InteractiveService interactiveService;
  private final ConsoleInputHandler inputHandler;
  private final List<String> availableApis;

  public InteractiveMode(ApiService apiService, FileService fileService, JsonUtil jsonUtil)
  {
    this(apiService, fileService, jsonUtil, null);
  }

  public InteractiveMode(ApiService apiService, FileService fileService, JsonUtil jsonUtil, InteractiveService interactiveService)
  {
    this.interactiveService = interactiveService != null ? interactiveService : new InteractiveService(apiService, fileService, jsonUtil);
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
        case 3 -> configurePolling();
        case 4 -> startPolling();
        case 5 -> stopPolling();
        case 6 -> exit = true;
        default -> System.out.println("Неверный ввод, попробуйте снова.");
      }
    }
    inputHandler.close();
  }

  private void printMenu()
  {
    System.out.println("\n--- Меню ---");
    System.out.println("1. Опрос API и сохранение в файл (однократно)");
    System.out.println("2. Вывести содержимое файла");
    System.out.println("3. Настройки периодического опроса");
    System.out.println("4. Запустить периодический опрос");
    System.out.println("5. Остановить периодический опрос");
    System.out.println("6. Выход");
    System.out.println("Текущие настройки: задачи=" + interactiveService.getPollingController().getMaxThreads() +
        ", интервал=" + interactiveService.getPollingController().getInterval() + " сек");
    if (interactiveService.getPollingController().isPolling())
    {
      System.out.println("** Опрос активен **");
    }
  }

  private void fetchAndSave()
  {
    System.out.println("Доступные API: " + availableApis);
    String chosen = inputHandler.readString("Введите названия API через запятую (или 'all' для всех): ");
    List<String> apisToFetch;
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
    Map<String, Map<String, String>> params = new HashMap<>();
    for (String api : apisToFetch)
    {
      System.out.println("Введите параметры для " + api);
      printApiParamsHint(api);
      String paramLine = inputHandler.readString("");
      if (!paramLine.isBlank())
      {
        Map<String, String> map = new HashMap<>();
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
    int maxThreads = interactiveService.getPollingController().getMaxThreads();
    List<AggregatedRecord> records = interactiveService.fetchFromApisParallel(apisToFetch, params, maxThreads);
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

  private void printApiParamsHint(String apiName)
  {
    switch (apiName.toLowerCase())
    {
      case "chucknorris":
        System.out.println("  Поддерживаемые параметры: category, name");
        System.out.println("  (параметры необязательны, можно оставить пустым)");
        System.out.println("Пример: category=animal,name=Chuck");
        break;
      case "zippopotam":
        System.out.println("  Обязательный параметр: zip");
        System.out.println("Пример: zip=90210");
        break;
      case "randomuser":
        System.out.println("  Возможные параметры: gender (male/female), nat (us,gb,fr и т.д.)");
        System.out.println("  Пример: gender=male,nat=us");
        break;
      default:
        System.out.println("  (параметры не задокументированы, оставьте пустым)");
        break;
    }
    System.out.print("Введите параметры (ключ=значение, через запятую) или оставьте пустым: ");
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

  private void configurePolling()
  {
    int maxThreads = inputHandler.readInt("Максимальное количество одновременно выполняемых задач: ");
    while (maxThreads <= 0)
    {
      System.out.println("Значение должно быть положительным.");
      maxThreads = inputHandler.readInt("Максимальное количество одновременно выполняемых задач: ");
    }
    long interval = inputHandler.readInt("Интервал опроса в секундах (0 - однократно): ");
    while (interval < 0)
    {
      System.out.println("Интервал не может быть отрицательным.");
      interval = inputHandler.readInt("Интервал опроса в секундах: ");
    }
    interactiveService.getPollingController().setMaxThreads(maxThreads);
    interactiveService.getPollingController().setInterval(interval);
    System.out.println("Настройки сохранены.");
  }

  private void startPolling()
  {
    if (interactiveService.getPollingController().isPolling())
    {
      System.out.println("Опрос уже запущен. Остановите его перед новым запуском.");
      return;
    }
    long interval = interactiveService.getPollingController().getInterval();
    if (interval <= 0)
    {
      System.out.println("Интервал опроса не задан или равен 0. Настройте интервал в меню 3.");
      return;
    }
    System.out.println("Доступные API: " + availableApis);
    String chosen = inputHandler.readString("Введите названия API через запятую (или 'all' для всех): ");
    List<String> apisToFetch;
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
    Map<String, Map<String, String>> params = new HashMap<>();
    for (String api : apisToFetch)
    {
      System.out.println("Введите параметры для " + api);
      printApiParamsHint(api);
      String paramLine = inputHandler.readString("");
      if (!paramLine.isBlank())
      {
        Map<String, String> map = new HashMap<>();
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
    String format = inputHandler.readString("Формат файла (json/csv): ");
    while (!format.equalsIgnoreCase("json") && !format.equalsIgnoreCase("csv"))
    {
      format = inputHandler.readString("Неверный формат. Введите json или csv: ");
    }
    String filePath = inputHandler.readString("Путь к файлу: ");
    boolean append = inputHandler.readBoolean("Дозаписать в существующий файл? (y/n): ");
    interactiveService.getPollingController().startPolling(apisToFetch, params, format, filePath, append);
  }

  private void stopPolling()
  {
    interactiveService.getPollingController().stopPolling();
  }
}
