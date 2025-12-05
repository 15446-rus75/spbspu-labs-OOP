package main;

import proc.*;

import java.util.*;

public class Main
{

  public static void main(String[] args)
  {
    List<Integer> numbers1 = Arrays.asList(1, 2, 3, 4, 5);
    System.out.println("Среднее значение: " + ProcessCollection.average(numbers1));

    List<String> strings1 = Arrays.asList("hello", "world", "java");
    System.out.println("Строки с префиксом: " + ProcessCollection.toUpperCaseWithPrefix(strings1));

    List<Integer> numbers2 = Arrays.asList(1, 2, 2, 3, 4, 4, 5);
    System.out.println("Квадраты уникальных элементов: " + ProcessCollection.squaresOfUniqueElements(numbers2));

    Collection<String> strings2 = Arrays.asList("apple", "banana", "apricot", "cherry", "avocado");
    System.out.println("Строки на 'a': " + ProcessCollection.filterAndSort(strings2, 'a'));

    List<String> strings3 = Arrays.asList("first", "second", "third");
    System.out.println("Последний элемент: " + ProcessCollection.getLastElement(strings3));

    int[] numbers3 = {1, 2, 3, 4, 5, 6};
    System.out.println("Сумма четных чисел: " + ProcessCollection.sumOfEvenNumbers(numbers3));

    List<String> strings4 = Arrays.asList("apple", "banana", "avocado", "berry");
    System.out.println("Map из строк: " + ProcessCollection.stringsToMap(strings4));
  }
}
