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
      System.out.print("Введите путь к файлу со словарем: ");
      String dictPath = scanner.nextLine().trim();
      if (dictPath.isEmpty())
      {
        dictPath = "src/translator/dict.txt";
      }
      TreeMap<String, String> dictionary = Dictionary.loadFromFile(dictPath);
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
