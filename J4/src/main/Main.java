package main;

import translator.exceptions.*;
import translator.*;
import java.util.TreeMap;
import java.util.Scanner;

public class Main
{
  public static void main(String[] args)
  {
    try
    {
      Scanner scanner = new Scanner(System.in);
      TreeMap<String, String> dictionary = Dictionary.loadFromConsole();
      System.out.print("Введите путь к файлу с текстом для перевода: ");
      String filePath = scanner.nextLine().trim();
      if (filePath.isEmpty())
      {
        filePath = "src/translator/text.txt";
      }
      Translator.translate(filePath, dictionary);
    }
    catch (Exception e)
    {
      System.err.println("ERROR: " + e.getMessage());
    }
  }
}
