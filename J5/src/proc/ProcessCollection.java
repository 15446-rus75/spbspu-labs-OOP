package stream_demo;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProcessCollection
{
  public static OptionalDouble average(List<Integer> numbers)
  {
    if (numbers == null)
    {
      throw new IllegalArgumentException("Список чисел не может быть null");
    }
    return numbers.stream()
           .mapToInt(Integer::intValue)
           .average();
  }

  public static List<String> toUpperCaseWithPrefix(List<String> strings)
  {
    if (strings == null)
    {
      throw new IllegalArgumentException("Список строк не может быть null");
    }
    return strings.stream()
                  .map(str -> "_new_" + str.toUpperCase())
                  .collect(Collectors.toList());
  }

  public static List<Integer> squaresOfUniqueElements(List<Integer> numbers)
  {
    if (numbers == null)
    {
      throw new IllegalArgumentException("Список чисел не может быть null");
    }
    return numbers.stream()
                  .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                  .entrySet()
                  .stream()
                  .filter(entry -> entry.getValue() == 1)
                  .map(entry -> entry.getKey() * entry.getKey())
                  .collect(Collectors.toList());
   }

  public static List<String> filterAndSort(Collection<String> strings, char startingLetter)
  {
    if (strings == null)
    {
      throw new IllegalArgumentException("Коллекция строк не может быть null");
    }
    return strings.stream()
                  .filter(str -> str != null && !str.isEmpty() && str.charAt(0) == startingLetter)
                  .sorted()
                  .collect(Collectors.toList());
    }

  public static <T> T getLastElement(Collection<T> collection)
  {
    if (collection == null)
    {
      throw new IllegalArgumentException("Коллекция не может быть null");
    }
    return collection.stream()
                     .reduce((first, second) -> second)
                     .orElseThrow(() -> new NoSuchElementException("Collection is empty"));
    }
}
