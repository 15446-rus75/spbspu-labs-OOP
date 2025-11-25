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
}
