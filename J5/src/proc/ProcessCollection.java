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
}
